/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives;

import org.renjin.eval.*;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.*;
import org.renjin.sexp.Vector;

import java.util.*;

/**
 * Primitives used in the implementation of the S3 object system
 */
public class S3 {

  public static final Symbol METHODS_TABLE = Symbol.get(".__S3MethodsTable__.");

  public static final Set<String> GROUPS = Sets.newHashSet("Ops", "Math", "Summary");

  @Builtin
  public static SEXP UseMethod(@Current Context context, String genericMethodName) {
    /*
     * When object is not provided, it defaults to the first argument
     * of the calling function
     */
    if(context.getArguments().length() == 0) {
      return UseMethod(context, genericMethodName, Null.INSTANCE);

    } else {
      SEXP object = context.evaluate( context.getArguments().getElementAsSEXP(0),
              context.getParent().getEnvironment());

      return UseMethod(context, genericMethodName, object);
    }
  }

  @Builtin
  public static SEXP UseMethod(@Current Context context, String genericMethodName, SEXP object) {


    return Resolver
              .start(context, genericMethodName, object)
              .withDefinitionEnvironment(context.getClosure().getEnclosingEnvironment())
              .next()
              .apply(context, context.getEnvironment());
  }

  @Internal
  public static SEXP NextMethod(@Current Context context, @Current Environment env,
      SEXP generic, SEXP object, @ArgumentList ListVector extraArgs) {

    return Resolver
           .resume(context)
           .withGenericArgument(generic)
           .withObjectArgument(object)
           .next()
           .applyNext(context, context.getEnvironment(), extraArgs);
  }


  /**
   * Computes the class list used for normal S3 Dispatch. Note that this
   * is different than the class() function
   */
  public static StringVector computeDataClasses(Context context, SEXP exp) {

    /*
     * Make sure we're dealing with the evaluated expression
     */
    exp = exp.force(context);

    SEXP classAttribute = exp.getAttribute(Symbols.CLASS);

    if(classAttribute.length() > 0) {
      /*
       * S3 Class has been explicitly defined
       */
      return (StringVector)classAttribute;
    } else {
      /*
       * Compute implicit class based on DIM attribute and type
       */
      StringArrayVector.Builder dataClass = new StringArrayVector.Builder();
      SEXP dim = exp.getAttribute(Symbols.DIM);
      if(dim.length() == 2) {
        dataClass.add("matrix");
      } else if(dim.length() == 1) {
        dataClass.add("array");
      }
      if(exp instanceof IntVector || exp instanceof DoubleVector) {
        dataClass.add(exp.getTypeName());
        dataClass.add("numeric");
      } else {
        dataClass.add(exp.getImplicitClass());
      }
      return dataClass.build();
    }
  }

  public static SEXP dispatchGroup(String group, FunctionCall call, String opName, PairList args, Context context, Environment rho) {
    int i, j, nargs;

    /*
     * First we need to see if we've already been through this.
     * (It's complicated)
     * 
     * - This method is called by "generic" primitives that, before doing 
     *   their normal thing, check to see if there's a user-defined function
     *   out there that provides specific behavior for the object. 
     * 
     * - It can happen that that user function in turn calls NextMethod() 
     *   to defer to the default behavior of the primitive. In this case,
     *   we don't want to check again because we've already been through
     *   that; to do so would be to loop infinitely.
     *   
     * - We can only tell whether this is the first or second time around,
     *   because if it's the second, NextMethod() will invoke the primitive
     *   by the name "<primitive>.default", like "as.character.default". 
     */
    if(call.getFunction() instanceof Symbol && 
        ((Symbol) call.getFunction()).getPrintName().endsWith(".default")) {
      return null;
    }


    boolean isOps = group.equals("Ops");

    if (isOps) {
      nargs = args.length();
    } else {
      nargs = 1;
    }

    GenericMethod left = Resolver.start(context, group, opName, args.getElementAsSEXP(0))
        .withBaseDefinitionEnvironment()
        .findNext();
        
    GenericMethod right = null;
    if(nargs == 2) {
      right = Resolver.start(context, group, opName, args.getElementAsSEXP(1))
          .withBaseDefinitionEnvironment()
          .findNext();
    }
    
    if(left == null && right == null) {
      // no generic method found
      return null;
    }
    
    if(left == null) {
      left = right;
    }

    /* we either have a group method or a class method */

    String[] m = new String[nargs];
    for (i = 0; i < nargs; i++) {
      StringVector t = computeDataClasses(context, args.getElementAsSEXP(i));

      boolean set = false;
      for (j = 0; j < t.length(); j++) {
        if ( t.getElementAsString(j).equals(left.className )) {
          m[i] = left.method.getPrintName();
          set = true;
          break;
        }
      }
      if (!set) {
        m[i] = "";
      }
    }
    left.withMethodVector(m);
    

    /* the arguments have been evaluated; since we are passing them */
    /* out to a closure we need to wrap them in promises so that */
    /* they get duplicated and things like missing/substitute work. */

    PairList promisedArgs = Calls.promiseArgs(call.getArguments(), context, rho);
    if (promisedArgs.length() != args.length()) {
      throw new EvalException("dispatch error in group dispatch");
    }
    if(promisedArgs != Null.INSTANCE) {
      PairList.Node promised = (PairList.Node)promisedArgs;
      PairList.Node evaluated = (PairList.Node)args;

      while(true) {

        ((Promise)promised.getValue()).setResult(evaluated.getValue());
        /* ensure positional matching for operators */
        if (isOps) {
          promised.setTag(Null.INSTANCE);
        }
        if(!promised.hasNextNode()) {
          break;
        }
        promised = promised.getNextNode();
        evaluated = evaluated.getNextNode();
      }
    }
    return left.doApply(context, rho, promisedArgs);
//    return Calls.applyClosure((Closure) left.sxp, context, newCall, promisedArgs, rho, newrho);
  }

  /**
   * There are a few primitive functions (`[[` among them) which are proper builtins, but attempt
   * to dispatch on the class of their first argument before going ahead with the default implementation.
   *
   * @param name the name of the function
   * @param args the original args from the FunctionCall
   * @param object evaluated first argument
   */
  public static SEXP tryDispatchFromPrimitive(Context context, Environment rho, FunctionCall call,
      String name, SEXP object, PairList args) {

    if(call.getFunction() instanceof Symbol &&
        ((Symbol)call.getFunction()).getPrintName().endsWith(".default")) {
      return null;
    }

    String methodEnvironmentName = ".__T__" + name + ":base";
    if(object instanceof S4Object && context.getGlobalEnvironment().getSymbolNames().contains(Symbol.get(methodEnvironmentName))) {
      return handleS4object(context, object, methodEnvironmentName, args, rho, 0, null);
    }

    GenericMethod method = Resolver
        .start(context, name, object)
        .withBaseDefinitionEnvironment()
        .withObjectArgument(object)
        .withGenericArgument(name)
        .findNext();

    if(method == null) {
      return null;
    }

    PairList newArgs = reassembleAndEvaluateArgs(object, args, context, rho);
    
    return method.doApply(context, rho, newArgs);
  }

  private static SEXP handleS4object(@Current Context context, SEXP source, String methodEnvName, PairList args,
                                     Environment rho, int argumentIndex, ArrayList<String> allArgClasses) {
    // for now the namespace is hardcoded (":base") since all primitives are from the base package. But to
    // allow this function to also work with standardGeneric the namespace needs to be identified at runtime
    // since these standardGenerics migh be in .GlobalEnv or elsewhere.
    Environment functionEnv = (Environment) context.getGlobalEnvironment().findVariable(context, Symbol.get(methodEnvName));
    SEXP function = null;
    // S4 methods for each generic function is stored in an environment. methods for each signature is stored
    // separately using the signature as name. for example
    // setMethod("[", signature("AA","BB","CC"), function(x, i, j, ...))
    // is stored as `AA#BB#CC` in an environment named `.__T__[:base`
    // here we get the first method from the method environment and split the name by # to know what the expected
    // signature length is. This might be longer the length of arguments and #ANY should be used for missing
    // arguments. In case signature is shorted than the number of arguments we don't need to evaluate the extra
    // arguments.
    int signatureLength = functionEnv.getFrame().getSymbols().iterator().next().getPrintName().split("#").length;

    ArrayList<SignatureDist> allPossibleSignatures = generateAllPossibleSignatures(context, rho, args, signatureLength);
    Collections.sort(allPossibleSignatures);

    ArrayList<SEXP> methods = findFunctionMatchingAnyOfSignatures(context, functionEnv, allPossibleSignatures);

    if(methods.isEmpty()) {
      throw new EvalException("object of type 'S4' is not subsettable");
    }

    return context.evaluate(new FunctionCall(methods.get(0), args));
  }

  private static ArrayList<SEXP> findFunctionMatchingAnyOfSignatures(Context context, Environment functionEnv, ArrayList<SignatureDist> signatures) {
    // recursively go through arguments and get their class and lookup until the signature is found.
    // add the found method and its rank to the map.

    ArrayList<SEXP> methods = new ArrayList<>();

    for(int signatureIndex = 0; signatureIndex < signatures.size(); ++signatureIndex) {
      String currentSig = signatures.get(signatureIndex).getSignature();
      Symbol methodName = Symbol.get(currentSig);
      SEXP function = functionEnv.findVariable(context, methodName);

      if(function != Symbol.UNBOUND_VALUE) {
        methods.add(function);
      }
    }

    return methods;
  }

  public static ArrayList<SignatureDist> generateAllPossibleSignatures(Context context, Environment rho,
                                                              PairList args, int depth) {

    String[] argsClass = new String[depth];
    ArgumentSig[] argumentSigs = new ArgumentSig[depth];

    for(int i = 0; i < depth; i++) {
      String currentClass = evaluateAndGetClass(context, args, rho, i);
      argsClass[i] = currentClass;
      argumentSigs[i] = getAllClassAndDistance(context, currentClass, 0);
    }

    int maxSigSize = 1;
    for(int i = 0; i < argumentSigs.length; i++) {
      maxSigSize = maxSigSize * argumentSigs[i].getArgument().length;
    }

    ArrayList<SignatureDist> sigDist = new ArrayList<>(maxSigSize);

    int currArgumentClassesLength;
    int currSigIdx = 0;
    int repeat = 1;
    int r = 1;

    for(int col = 0; col < depth; col++) {
      currArgumentClassesLength = argumentSigs[col].getArgument().length;
      for(int row = 0; row < maxSigSize; row++, r++) {
        if(currSigIdx == currArgumentClassesLength) {
          currSigIdx = 0;
        }
        ArgumentSig argSig = argumentSigs[col];
        if(sigDist.isEmpty() || sigDist.toArray().length < row+1 || sigDist.toArray()[row] == null) {
          sigDist.add(row, new SignatureDist(argSig.getArgument(currSigIdx), argSig.getDistanceAsArray(currSigIdx)));
        } else {
          sigDist.set(row, sigDist.get(row).append(argSig.getArgument(currSigIdx), argSig.getDistance(currSigIdx)));
        }
        if(repeat == 1) {
          currSigIdx++;
        }
        if(repeat != 1 && r == repeat) {
          r = 0;
          currSigIdx++;
        }
      }
      r = 1;
      currSigIdx = 0;
      repeat = repeat * currArgumentClassesLength;
    }
    return sigDist;
  }

  public static String evaluateAndGetClass(Context context, PairList args, Environment rho, int argumentIndex) {
    // To get the class of an argument we have to evaluate the argument first. If its an atomic, than it lacks class
    // attribute. In this case we use Attributes.getClass() to get the class name.
    String className;
    SEXP evaluatedArg = context.evaluate(args.getElementAsSEXP(argumentIndex), rho);
    if(evaluatedArg.getAttributes().hasClass()) {
      className = evaluatedArg.getAttributes().getClassVector().getElementAsString(0);
    } else {
      className = Attributes.getClass(evaluatedArg).getElementAsString(0);
    }
    return className;
  }

  public static ArgumentSig getAllClassAndDistance(Context context, String argClass, int signatureLength) {
    Symbol argClassObjectName = Symbol.get(".__C__" + argClass);
    Frame globalFrame = context.getGlobalEnvironment().getFrame();
    AttributeMap map = globalFrame.getVariable(argClassObjectName).getAttributes();
    SEXP containsSlot = map.get("contains");
    SEXP argSuperClasses = containsSlot.getNames();
    int[] distances = new int[argSuperClasses.length() + 2];
    String[] classes = new String[argSuperClasses.length() + 2];
    classes[0] = argClass;
    distances[0] = 0;
    for(int i = 0; i < argSuperClasses.length(); i++) {
      DoubleArrayVector distanceSlot = (DoubleArrayVector) ((ListVector) containsSlot).get(i).getAttributes().get("distance");
      distances[i + 1] = distanceSlot.getElementAsInt(0);
      classes[i + 1] = ((StringArrayVector) argSuperClasses).getElementAsString(i);
    }

    int max = 0;
    for(int i = 0; i < distances.length; i++) {
      if(distances[i] > max) {
        max = distances[i];
      }
    }

    distances[argSuperClasses.length() + 1] = max + 1;
    classes[distances.length - 1] = "ANY";

    return new ArgumentSig(classes, distances);
  }

  public static SEXP tryDispatchFromPrimitive(Context context, Environment rho, FunctionCall call,
      String name, String[] argumentNames, SEXP[] arguments) {

    if(call.getFunction() instanceof Symbol &&
        ((Symbol)call.getFunction()).getPrintName().endsWith(".default")) {
      return null;
    }

    Vector classVector = (Vector)arguments[0].getAttribute(Symbols.CLASS);
    if(classVector.length() == 0) {
      return null;
    }

    DispatchChain chain = DispatchChain.newChain(context, rho, name, classVector);
    if(chain == null) {
      return null;
    }

    PairList.Builder newArgsBuilder = new PairList.Builder();
    for(int i=0;i!=arguments.length;++i) {
      newArgsBuilder.add(argumentNames[i], arguments[i]);
    }
    PairList newArgs = newArgsBuilder.build();

    FunctionCall newCall = new FunctionCall(chain.getMethodSymbol(), newArgs);

    ClosureDispatcher dispatcher = new ClosureDispatcher(context, rho, newCall);
    return dispatcher.apply(chain, newArgs);
  }

  /**
   * Evaluate the remaining arguments to the primitive call. Despite the fact that we are passing these
   * arguments to a user-defined closure, we _still_ evaluate arguments eagerly.
   */
  static PairList reassembleAndEvaluateArgs(SEXP object, PairList args, Context context, Environment rho) {
    PairList.Builder newArgs = new PairList.Builder();
    PairList.Node firstArg = (PairList.Node)args;
    newArgs.add(firstArg.getRawTag(), object);

    ArgumentIterator argIt = new ArgumentIterator(context, rho, firstArg.getNext());
    while(argIt.hasNext()) {
      PairList.Node node = argIt.nextNode();
      newArgs.add(node.getRawTag(), context.evaluate(node.getValue(), rho));
    }
    return newArgs.build();
  }

  public static SEXP tryDispatchOpsFromPrimitive(Context context, Environment rho, FunctionCall call, 
                                                 String name, SEXP s0) {

    PairList newArgs = new PairList.Node(s0, Null.INSTANCE);

    return dispatchGroup("Ops", call, name, newArgs, context, rho);
  }

  public static SEXP tryDispatchOpsFromPrimitive(Context context, Environment rho, FunctionCall call, 
                                                 String name, SEXP s0, SEXP s1) {

    PairList newArgs = new PairList.Node(s0, new PairList.Node(s1, Null.INSTANCE));

    return dispatchGroup("Ops", call, name, newArgs, context, rho);
  }

  public static SEXP tryDispatchGroupFromPrimitive(Context context, Environment rho, FunctionCall call,
                                                 String group, String name, SEXP s0, PairList args) {

    // Add our first, already evaluated argument
    PairList.Node firstNode = (PairList.Node) args;
    PairList newArgs = new PairList.Node(s0, firstNode.getNext());

    return dispatchGroup(group, call, name, newArgs, context, rho);
  }



  public static SEXP tryDispatchSummaryFromPrimitive(Context context, Environment rho, FunctionCall call,
      String name, ListVector evaluatedArguments, boolean naRm) {

    // REpackage the evaluated arguments.
    // this is ghastly but i don't think it will
    // get better until Calls is refactored

    PairList.Builder newArgs = new PairList.Builder();
    int varArgIndex = 0;
    Symbol naRmName = Symbol.get("na.rm");
    for(PairList.Node node : call.getArguments().nodes()) {
      if(node.getRawTag() == naRmName) {
        newArgs.add(node.getTag(), new LogicalArrayVector(naRm));
      } else {
        newArgs.add(node.getRawTag(), evaluatedArguments.get(varArgIndex++));
      }
    }

    return dispatchGroup("Summary", call, name, newArgs.build(), context, rho);
  }

  
  /**
   * Helper class to deal with all the messy details of resolving
   * generic methods.
   *
   */
  private static class Resolver {

    /**
     * The environment of the call to the generic method.
     */
    private Environment callingEnvironment;

    /**
     * The environment in which the generic was defined. This will be the
     * enclosing environment of the function that calls UseMethod().
     */
    private Environment definitionEnvironment = Environment.EMPTY;
    
    private String group;

    /**
     * The name of the generic: for example "print" or "summary" or
     * "as.character".
     */
    private String genericMethodName;

    /**
     * The S3 classes associated with this object, each to be tried
     * in turn
     */
    private List<String> classes;

    private Context context;

    private SEXP object;

    /**
     * The context of the *previous* generic method called,
     * or null if this is the first method dispatched from
     * UseMethod().
     */
    private Context previousContext;

    
    private static Resolver start(Context context, String genericMethodName, SEXP object) {
      return start(context, null, genericMethodName, object);
    }

    private static Resolver start(Context context, String group, String genericMethodName, SEXP object) {
      Resolver resolver = new Resolver();
      resolver.callingEnvironment = context.getEnvironment();
      resolver.genericMethodName = genericMethodName;
      resolver.context = context;
      resolver.object = object;
      resolver.group = group;
      resolver.classes = Lists.newArrayList(computeDataClasses(context, object));
      return resolver;
    }


    /**
     * Resumes a dispatch chain (called by NextMethod)
     */
    public static Resolver resume(Context context) {
      Context parentContext = findParentContext(context);
      GenericMethod method = parentContext.getState(GenericMethod.class);

      Resolver resolver = new Resolver();
      resolver.context = context;
      resolver.previousContext = parentContext;
      resolver.callingEnvironment = context.getEnvironment();
      resolver.definitionEnvironment = method.resolver.definitionEnvironment;
      resolver.genericMethodName = method.resolver.genericMethodName;
      resolver.classes = method.nextClasses();
      resolver.group = method.resolver.group;
      resolver.object = method.resolver.object;
      return resolver;
    }


    public Resolver withObjectArgument(SEXP object) {
      if(object != Null.INSTANCE) {
        this.object = object;
      }
      return this;
    }

    /**
     * Sets the name of the generic method to resolve from 
     * an argument provided to UseMethod(). 
     * 
     * @param generic the 'generic' argument, giving the name of the method, like
     * 'as.character', or 'print'.
     */
    public Resolver withGenericArgument(SEXP generic) {
      if(generic != Null.INSTANCE) {
        this.genericMethodName = generic.asString();
      }
      return this;
    }
    
    /**
     * Sets the name of the generic method to resolve from 
     * an argument provided to UseMethod(). 
     * 
     * @param genericName the 'generic' argument, giving the name of the method, like
     * 'as.character', or 'print'.
     */
    public Resolver withGenericArgument(String genericName) {
      this.genericMethodName = genericName;
      return this;
    }

    public Resolver withDefinitionEnvironment(Environment rho) {
      this.definitionEnvironment = rho;
      return this;
    }

    public Resolver withBaseDefinitionEnvironment() {
      this.definitionEnvironment = context.getBaseEnvironment();
      return this;
    }

    private static Context findParentContext(Context context) {
      for( ; context != null; context = context.getParent() ) {
        if(context.getState(GenericMethod.class) != null) {
          return context;
        }
      }
      throw new EvalException("NextMethod called out of context");
    }

    public GenericMethod next() {

      GenericMethod next = findNextOrDefault();

      if(next == null) {
        throw new EvalException("no applicable method for '%s' applied to an object of class \"%s\"",
          genericMethodName, classes.toString());
      }

      return next;
    }

    private GenericMethod findNextOrDefault() {
      GenericMethod next = findNext();

      if(next != null) {
        return next;
      } else {
        Environment methodTable = getMethodTable();
        GenericMethod function = findNext(methodTable, genericMethodName, "default");
        if(function != null) {
          return function;
        }

        // as a last step, we call BACK into the primitive
        // to get the default implementation  - ~ YECK ~
        PrimitiveFunction primitive = Primitives.getBuiltin(genericMethodName);
        if(primitive != null) {
          return new GenericMethod(this, Symbol.get(genericMethodName + ".default"), null, primitive);
        }

        return null;
      }
    }

    public GenericMethod findNext() {
      Environment methodTable = getMethodTable();
      GenericMethod method;

      for(String className : classes) {
        
        method = findNext(methodTable, genericMethodName, className);
        if(method != null) {
          return method;
        }
        if(group != null) {
          method = findNext(methodTable, group, className);
          if(method != null) {
            return method;
          }
        }
      }
      return null;
    }

    private GenericMethod findNext(Environment methodTable, String name, String className) {
      Symbol method = Symbol.get(name + "." + className);
      SEXP function = callingEnvironment.findFunction(context, method);
      if(function != null) {
        return new GenericMethod(this, method, className, (Function) function);
        
      } else if(methodTable != null && methodTable.hasVariable(method)) {
        return new GenericMethod(this, method, className, (Function) methodTable.getVariableUnsafe(method).force(context));
      
      } else {
        return null;
      }
    }

    private Environment getMethodTable() {
      SEXP table = definitionEnvironment.getVariableUnsafe(METHODS_TABLE).force(context);
      if(table instanceof Environment) {
        return (Environment) table;
      } else if(table == Symbol.UNBOUND_VALUE) {
        return null;
      } else {
        throw new EvalException("Unexpected value for .__S3MethodsTable__. in " + definitionEnvironment.getName());
      }
    }
  }

  public static class GenericMethod {
    private Resolver resolver;

    /**
     * The name of the selected function
     */
    private Symbol method;
    private Function function;
    private String className;
    
    /**
     * The vector stored in .Method
     * 
     * <p>Normally it is just a single string containing the 
     * name of the selected method, but for Ops group members,
     * it a two element string vector of the form 
     * 
     * [ "Ops.factor", ""]
     * [ "", "Ops.factor"] or
     * [ "Ops.factor", "Ops.factor"] 
     * 
     * depending on which (or both) of the operands belong to 
     * the selected class.
     * 
     */
    private StringVector methodVector;

    public GenericMethod(Resolver resolver, Symbol method, String className, Function function) {
      assert function != null;
      this.resolver = resolver;
      this.method = method;
      this.methodVector = new StringArrayVector(method.getPrintName());
      this.className = className;
      this.function = function;
    }

    public SEXP apply(Context callContext, Environment callEnvironment) {
      PairList rePromisedArgs = Calls.promiseArgs(callContext.getArguments(), callContext, callEnvironment);
      return doApply(callContext, callEnvironment, rePromisedArgs);
    }

    public SEXP applyNext(Context context, Environment environment, ListVector extraArgs) {
      PairList arguments = nextArguments(context, extraArgs);

      if("Ops".equals(resolver.group) && arguments.length() == 2) {
        withMethodVector(groupsMethodVector());
      }

      return doApply(context, environment, arguments);
    }

    private String[] groupsMethodVector() {
      GenericMethod previousMethod = resolver.previousContext.getState(GenericMethod.class);
      String methodVector[] = previousMethod.methodVector.toArray();

      String methodName = this.methodVector.getElementAsString(0);

      for (int i = 0; i < methodVector.length; i++) {
        if(!methodVector[i].equals("")) {
          methodVector[i] = methodName;
        }
      }
      return methodVector;
    }

    public SEXP doApply(Context callContext, Environment callEnvironment, PairList args) {
      FunctionCall newCall = new FunctionCall(method,args);

      callContext.setState(GenericMethod.class, this);

      if(Profiler.ENABLED) {
        Profiler.functionStart(this.method, function);
      }
      try {
        if (function instanceof Closure) {
          // Note that the callingEnvironment or "sys.parent" of the selected function will be the calling 
          // environment of the wrapper function that calls UseMethod, NOT the environment in which UseMethod
          // is evaluated.
          Environment callingEnvironment = callContext.getCallingEnvironment();
          if(callingEnvironment == null) {
            callingEnvironment = callContext.getGlobalEnvironment();
          }
          return Calls.applyClosure((Closure) function, callContext, callingEnvironment, newCall,
              args, persistChain());
        } else {
          // primitive
          return function.apply(callContext, callEnvironment, newCall, args);
        }
      } finally {
        
        callContext.clearState(GenericMethod.class);
        
        if(Profiler.ENABLED) {
          Profiler.functionEnd();
        }
      }
    }
    
    public GenericMethod withMethodVector(String[] methodNames) {
      this.methodVector = new StringArrayVector(methodNames);
      return this;
    }

    public PairList nextArguments(Context callContext, ListVector extraArgs) {

      /*
       * Get the arguments to the function that called NextMethod(),
       * no arguments are really passed to NextMethod() at all.
       * 
       * Note that we have to do a bit of climbing here-- it can be that previous 
       * method looked like:
       * 
       * `[.simple.list <- function(x, i, ...) structure(NextMethod('['], class=class(x))
       * 
       * In this case, NextMethod is passed a promise to the closure 'structure',
       * and so our parent context is NOT the function context of `[.simple.list`
       * but that of `structure`. 
       * 
       */
      Context parentContext = callContext.getParent();
      while(parentContext.getParent() != resolver.previousContext) {
        parentContext = parentContext.getParent();
      }

      /*
       * Now update the original arguments with any new values from the previous generic.
       * in the chain. To do this, we have to match the original arguments to the
       * formal names of the previous generic.
       */

      PairList actuals = parentContext.getArguments();
      Closure closure = parentContext.getClosure();
      PairList formals = closure.getFormals();
      Environment previousEnv = parentContext.getEnvironment();

      return updateArguments(parentContext, actuals, formals, previousEnv, extraArgs);
    }


    private Frame persistChain() {
      HashFrame frame = new HashFrame();
      frame.setVariable(Symbol.get(".Class"), new StringArrayVector(resolver.classes));
      frame.setVariable(Symbol.get(".Method"), methodVector);
      frame.setVariable(Symbol.get(".Generic"), StringVector.valueOf(resolver.genericMethodName));
      frame.setVariable(Symbol.get(".GenericCallEnv"), resolver.callingEnvironment);
      frame.setVariable(Symbol.get(".GenericDefEnv"), resolver.definitionEnvironment);
      return frame;
    }

    @Override
    public String toString() {
      return method + "." + className;
    }

    /**
     *
     * @return remaining classes to be  tried after this method
     */
    public List<String> nextClasses() {
      if(className == null) {
        return Collections.emptyList();
      }
      int myIndex = resolver.classes.indexOf(className);
      return resolver.classes.subList(myIndex+1, resolver.classes.size());
    }
  }

  public static PairList updateArguments(Context context, PairList actuals, PairList formals,
                                         Environment previousEnv, ListVector extraArgs) {
    // match each actual to a formal name so we can update it's value. but we can't reorder!

    List<SEXP> actualNames = Lists.newArrayList();
    List<SEXP> actualValues = Lists.newArrayList();
    List<Symbol> matchedNames = Lists.newArrayList();


    List<PairList.Node> unmatchedFormals = Lists.newLinkedList(formals.nodes());


    // unpack ... and match exactly
    for (PairList.Node node : actuals.nodes()) {
      if(node.getValue() instanceof  PromisePairList) {
        PromisePairList ellipses = (PromisePairList) node.getValue();
        for(PairList.Node nestedNode : ellipses.nodes()) {
          actualNames.add(nestedNode.getRawTag());
          actualValues.add(nestedNode.getValue());
          matchedNames.add(matchArgumentExactlyByName(nestedNode.getRawTag(), unmatchedFormals));
        }

      } else {
        actualNames.add(node.getRawTag());
        actualValues.add(node.getValue());
        matchedNames.add(matchArgumentExactlyByName(node.getRawTag(), unmatchedFormals));
      }
    }

    // match partially
    for (int i = 0; i!=matchedNames.size();++i) {
      if(matchedNames.get(i) == null) {
        matchedNames.set(i, matchPartiallyByName(actualNames.get(i), unmatchedFormals));
      }
    }

    // update
    Iterator<PairList.Node> formalIt = unmatchedFormals.iterator();
    for (int i = 0; i!=matchedNames.size();++i) {
      if(matchedNames.get(i) == null) {
        if(!formalIt.hasNext()) {
          throw new EvalException("Unmatched argument");
        }
        Symbol nextFormalName = formalIt.next().getTag();
        if(nextFormalName == Symbols.ELLIPSES) {
          // don't match this or any subsequent arguments
          break;
        } else {
          matchedNames.set(i, nextFormalName);
        }
      }
    }

    // update arguments from environment
    PairList.Builder updated = PairList.Node.newBuilder();

    for (int i = 0; i!=matchedNames.size();++i) {
      SEXP updatedValue;
      if(matchedNames.get(i) != null) {
        updatedValue = previousEnv.getVariableUnsafe(matchedNames.get(i));
        assert updatedValue != Symbol.UNBOUND_VALUE;
      } else {
        updatedValue = actualValues.get(i);
      }
      updated.add(actualNames.get(i), updatedValue);
    }
    
    // Add extra arguments passed to NextMethods
    for (NamedValue extraArg : extraArgs.namedValues()) {
      if(!extraArg.hasName()) {
        updated.add(extraArg.getValue());
      } else {
        // update any existing arguments by this name, or add
        updated.set(extraArg.getName(), extraArg.getValue());
      }
    }

    return updated.build();
  }

  private static Symbol matchArgumentExactlyByName(SEXP tag, List<PairList.Node> unmatchedFormals) {
    if(tag == Null.INSTANCE) {
      return null;
    } else {
      for(PairList.Node formal : unmatchedFormals) {
        if(formal.getTag() == tag) {
          unmatchedFormals.remove(formal);
          return formal.getTag();
        }
      }
    }
    return null;
  }

  private static Symbol matchPartiallyByName(SEXP tag, List<PairList.Node> unmatchedFormals) {
    if(tag == Null.INSTANCE) {
      return null;
    } else {
      String name = ((Symbol)tag).getPrintName();
      PairList.Node partialMatch = null;
      for(PairList.Node formal : unmatchedFormals) {
        if(formal.getTag().getPrintName().startsWith(name)) {
          if(partialMatch != null) {
            throw new EvalException("multiple partial matches");
          }
          partialMatch = formal;
        }
      }
      if(partialMatch == null) {
        return null;
      } else {
        return partialMatch.getTag();
      }
    }
  }

  public static class ArgumentSig {
    private String[] argument;
    private int[] distance;

    ArgumentSig(String[] arg, int[] dist) {
      this.argument = arg;
      this.distance = dist;
    }

    public String[] getArgument() {
      return argument;
    }

    public String getArgument(int position) {
      return this.argument[position];
    }

    public int getDistance(int position) {
      return this.distance[position];
    }

    public int[] getDistanceAsArray(int position) {
      int[] array = new int[1];
      array[0] = this.distance[position];
      return array;
    }
  }

  public static class SignatureDist implements Comparable <SignatureDist> {
    private String signature;
    private int[] distance;
    private boolean has0 = false;
    private int totalDist = 0;
    private double rank = 0.0;

    SignatureDist(String signature, int[] distance) {
      this.signature = signature;
      this.distance = distance;

      int totalDistance = 0;
      for(int i = 0; i < distance.length; i++) {
        this.rank = this.rank + ((10007.0 * Math.pow(0.5, (double) i)) * (double) distance[i]);
        if (distance[i] == 0) {
          this.has0 = true;
        } else {
          totalDistance = totalDistance + distance[i];
        }
      }
      this.totalDist = totalDistance;

    }

    public SignatureDist append(String argument, int distance) {
      String newSig = this.signature + "#" + argument;
      int[] newDist = new int[this.distance.length + 1];
      for(int i = 0; i < this.distance.length; i++) {
        newDist[i] = this.distance[i];
      }
      newDist[this.distance.length] = distance;
      this.signature = newSig;
      this.distance = newDist;
      this.has0 = (this.has0 == true || distance == 0);
      this.totalDist = this.totalDist + distance;
      this.rank = this.rank + ((10007.0 * Math.pow(0.5, (double) newDist.length)) * (double) distance);
      return this;
    }

    public String getSignature() {
      return signature;
    }

    public int[] getDistance() {
      return distance;
    }

    public int getTotalDist() {
      return totalDist;
    }

    public int isHas0() {
      if(has0){
        return 0;
      }
      return 1;
    }

    public double getRank() {
      return rank;
    }

    @Override
    public int compareTo(SignatureDist o) {
      int i = Integer.valueOf(this.getTotalDist()).compareTo(o.getTotalDist());
      if (i != 0) return i;

      i = Integer.valueOf(this.isHas0()).compareTo(o.isHas0());
      if (i != 0) return i;

      return Double.compare(rank, o.getRank());
    }
  }

}
