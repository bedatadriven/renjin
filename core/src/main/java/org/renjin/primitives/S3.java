/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.eval.*;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.s4.S4;
import org.renjin.sexp.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Primitives used in the implementation of the S3 object system
 */
public class S3 {
  public static final Set<String> GROUPS = Sets.newHashSet("Ops", "Math", "Summary");

  /**
   * The size of the dispatch table.
   *
   * <p>The dispatch table </p>
   */
  public static final int DISPATCH_TABLE_SIZE = 6;


  /**
   * ‘.Generic’ is a length-one character vector naming the generic function.
   */
  public static final int GENERIC_DISPATCH_TABLE_INDEX = 0;


  /**
   * ‘.Class’ is a character vector of classes used to find the next
   * method.  ‘NextMethod’ adds an attribute ‘"previous"’ to ‘.Class’
   * giving the ‘.Class’ last used for dispatch, and shifts ‘.Class’
   * along to that used for dispatch.
   */
  public static final int CLASS_DISPATCH_TABLE_INDEX = 1;

  /**
   * ‘.Method’ is a character vector (normally of length one) naming
   * the method function.  (For functions in the group generic ‘Ops’ it
   * is of length two.)
   */
  public static final int METHOD_DISPATCH_TABLE_INDEX = 2;

  public static final int GROUP_DISPATCH_TABLE_INDEX = 3;

  /**
   * ‘.GenericCallEnv’ is the environment of the
   * call to be generic.
   */
  public static final int GENERIC_CALL_ENV_TABLE_INDEX = 4;

  /**
   * ‘.GenericDefEnv’ is the environment of the
   * defining the generic respectively. It is
   * used to find methods registered for the generic
   */
  public static final int GENERIC_DEF_ENV_TABLE_INDEX = 5;

  public static final Symbol METHODS_TABLE = Symbol.get(".__S3MethodsTable__.");

  private static final Symbol NA_RM = Symbol.get("na.rm");


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

  public static StringVector computeObjectClass(Context context, SEXP object) {
    StringVector objectClasses = computeDataClasses(context, object);
    if(Types.isS4(object)) {
      SEXP objectClassesS4 = S4.computeDataClassesS4(context, objectClasses.getElementAsString(0));
      if (objectClassesS4 instanceof StringVector) {
        return (StringVector) objectClassesS4;
      }
    }
    return objectClasses;
  }

  /**
   * Attempts to compute the classes used for S3 dispatch based on value bounds for an expression.
   * 
   * @param valueBounds
   * @return a StringVector containing the known class list for this expression, or {@code null} if they could not
   * be deduced.
   */
  public static StringVector computeDataClasses(ValueBounds valueBounds) {

    // If we don't know what the value's class attribute is, we can't make
    // any further assumptions
    if(valueBounds.hasUnknownClassAttribute()) {
      return null;
    }

    if(valueBounds.isFlagSet(ValueBounds.MAYBE_CLASS)) {
      AtomicVector classAttribute = valueBounds.getConstantClassAttribute();
      if (classAttribute.length() > 0) {
        // S3 class has been explicitly defined and is constant at compile time
        return (StringVector) classAttribute;
      }
    }
    
    // Otherwise we compute based on the type and dimensions
    // So in the absence of a constant class attribute, these two
    // properties need to be constant.
    if(!valueBounds.isDimCountConstant()) {
      return null;
    }

    int typeSet = valueBounds.getTypeSet();
    String implicitClass = TypeSet.implicitClass(typeSet);
    if(implicitClass == null) {
      return null;
    }
    
    StringArrayVector.Builder dataClass = new StringArrayVector.Builder();

    if(valueBounds.isFlagSet(ValueBounds.HAS_DIM2)) {
      dataClass.add("matrix");
    } else if(valueBounds.isFlagSet(ValueBounds.HAS_DIM)) {
      dataClass.add("array");
    }
    
    dataClass.add(implicitClass);
    
    if((typeSet & TypeSet.NUMERIC) != 0) {
      dataClass.add("numeric");
    }
    return dataClass.build();
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
      } else if(dim.length() > 0) {
        dataClass.add("array");
      }
      if(exp instanceof IntVector) {
        dataClass.add("integer");
        dataClass.add("numeric");
      } else if(exp instanceof DoubleVector) {
        dataClass.add("double");
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

    GenericMethod left;
    for (int k = 0; k < nargs; k++) {
      if(Types.isS4(args.getElementAsSEXP(k))) {
        return S4.tryS4DispatchFromPrimitive(context, args.getElementAsSEXP(0), args, rho, group, opName);
      }
    }

    if(opName.equals("%*%")) {
      return null;
    }
    
    left = Resolver.start(context, rho, group, opName, args.getElementAsSEXP(0))
        .withBaseDefinitionEnvironment()
        .findNext();
        
    GenericMethod right = null;
    if(nargs == 2) {
      right = Resolver.start(context, rho, group, opName, args.getElementAsSEXP(1))
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

    PairList promisedArgs = Calls.promiseArgs(args, context, rho);
    if (promisedArgs.length() != args.length()) {
      throw new EvalException("dispatch error in group dispatch");
    }
    if(promisedArgs != Null.INSTANCE) {
      PairList.Node promised = (PairList.Node)promisedArgs;

      while(true) {

        /* The first argument has been evaluated, but not the rest */
        if(promised == promisedArgs) {
          ((Promise)promised.getValue()).setResult(((PairList.Node) args).getValue());
        }

        /* ensure positional matching for operators */
        if (isOps) {
          promised.setTag(Null.INSTANCE);
        }
        if(!promised.hasNextNode()) {
          break;
        }
        promised = promised.getNextNode();
      }
    }
    return left.doApply(context, rho, call, promisedArgs);
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

    SEXP resultS4Dispatch = null;
    if(Types.isS4(object) && isS4DispatchSupported(name)) {
      resultS4Dispatch = S4.tryS4DispatchFromPrimitive(context, object, args, rho, null, name);
    }
    if (resultS4Dispatch != null) {
      return resultS4Dispatch;
    }

    GenericMethod method = Resolver
        .start(context, rho, null, name, object)
        .withBaseDefinitionEnvironment()
        .withObjectArgument(object)
        .withGenericArgument(name)
        .findNext();

    if(method == null) {
      return null;
    }

    PairList newArgs = reassembleAndEvaluateArgs(object, args, context, rho);
    Context fakeContext = context.beginFunction(rho, call, new Closure(rho, Null.INSTANCE, Null.INSTANCE), args);
    return method.doApply(fakeContext, rho, call, newArgs);
  }

  private static boolean isS4DispatchSupported(String name) {
    return !("@<-".equals(name));
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
   * Wrap the remaining arguments to the primitive call in promises.
   */
  static PairList reassembleAndEvaluateArgs(SEXP object, PairList args, Context context, Environment rho) {
    PairList.Builder newArgs = new PairList.Builder();
    PairList.Node firstArg = (PairList.Node)args;
    newArgs.add(firstArg.getRawTag(), new Promise(firstArg.getValue(), object));

    ArgumentIterator argIt = new ArgumentIterator(context, rho, firstArg.getNext());
    while(argIt.hasNext()) {
      PairList.Node node = argIt.nextNode();
      if(node.getValue() == Symbol.MISSING_ARG) {
        newArgs.add(node.getRawTag(), Symbol.MISSING_ARG);
      } else {
        newArgs.add(node.getRawTag(), Promise.repromise(rho, node.getValue()));
      }
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

    // This call's arguments have been previously evaluated and parsed
    // by the function wrapper (for example, R$primitive$max) in preparation for
    // dispatching to the builtin method.

    // Now we need walk that work back in order to be able to dispatch
    // to a user-supplied closure override.

    PairList.Builder newArgs = new PairList.Builder();
    int varArgIndex = 0;
    boolean naRmArgumentSupplied = false;
    for(PairList.Node node : call.getArguments().nodes()) {
      if(node.getRawTag() == NA_RM) {
        newArgs.add(node.getTag(), new LogicalArrayVector(naRm));
        naRmArgumentSupplied = true;
      } else if(node.getValue() == Symbols.ELLIPSES) {
        // Add all remaining arguments
        while(varArgIndex < evaluatedArguments.length()) {
          newArgs.add(evaluatedArguments.getName(varArgIndex), evaluatedArguments.get(varArgIndex));
          varArgIndex++;
        }
      } else {
        newArgs.add(node.getRawTag(), evaluatedArguments.get(varArgIndex++));
      }
    }

    // When dispatching to S3 summary methods, we pretend that the summary
    // builtin has an extra na.rm argument with default value false.

    if(!naRmArgumentSupplied) {
      newArgs.add(NA_RM, LogicalVector.valueOf(naRm));
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
      return start(context, context.getEnvironment(), null, genericMethodName, object);
    }

    private static Resolver start(Context context, Environment rho, String group, String genericMethodName, SEXP object) {
      Resolver resolver = new Resolver();
      resolver.callingEnvironment = rho;
      resolver.genericMethodName = genericMethodName;
      resolver.context = context;
      resolver.object = object;
      resolver.group = group;
  
      StringVector objectClasses = computeDataClasses(context, object);
      if(Types.isS4(object)) {
        SEXP objectClassesS4 = S4.computeDataClassesS4(context, objectClasses.getElementAsString(0));
        if(objectClassesS4 != Null.INSTANCE) {
          List<String> classes = Lists.newArrayList(objectClasses);
          classes.addAll(Lists.newArrayList((StringVector)objectClassesS4));
          resolver.classes = classes;
        } else {
          resolver.classes = Lists.newArrayList(objectClasses);
        }
      } else {
        resolver.classes = Lists.newArrayList(objectClasses);
      }
      
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
      }

      // Look up the .default method first in the definition environment
      GenericMethod function = findNext(definitionEnvironment, genericMethodName, "default");
      if(function != null) {
        return function;
      }

      // Otherwise see if *another* package has defined a default method
      function = findNext(getMethodTable(), genericMethodName, "default");
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

      } else if(methodTable.hasVariable(method)) {
        return new GenericMethod(this, method, className, (Function) methodTable.getVariableUnsafe(method).force(context));

      } else {
        return null;
      }
    }

    private Environment getMethodTable() {
      return findMethodTable(context, definitionEnvironment);
    }

  }

  public static Environment findMethodTable(Context context, Environment definitionEnvironment) {
    SEXP table = definitionEnvironment.getVariableUnsafe(METHODS_TABLE).force(context);
    if(table instanceof Environment) {
      return (Environment) table;
    } else if(table == Symbol.UNBOUND_VALUE) {
      return Environment.EMPTY;
    } else {
      throw new EvalException("Unexpected value for .__S3MethodsTable__. in " + definitionEnvironment.getName());
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
      return doApply(callContext, callEnvironment, callContext.getCall(), rePromisedArgs);
    }

    public SEXP applyNext(Context context, Environment environment, ListVector extraArgs) {
      Context originalCallingContext = findOriginalCallingContext(context);
      PairList arguments = nextArguments(originalCallingContext, extraArgs);

      if("Ops".equals(resolver.group) && arguments.length() == 2) {
        withMethodVector(groupsMethodVector());
      }

      return doApply(context, environment, originalCallingContext.getCall(), arguments);
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

    public SEXP doApply(Context callContext, Environment callEnvironment, FunctionCall call, PairList promisedArgs) {


      // The new call that is visible to sys.call() and match.call()
      // is identical to the call which invoked UseMethod(), but we do update the function name.

      // For example, if you have a stack which looks like foo(x) -> UseMethod('foo') -> foo.default(x) then
      // the foo.default function will have a call of foo.default(x) visible to sys.call() and match.call()
      FunctionCall newCall = new FunctionCall(method, call.getArguments());

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
                  promisedArgs, persistChain());
        } else {
          // primitive
//          return function.apply(callContext, callEnvironment, newCall, promisedArgs);
          throw new UnsupportedOperationException("TODO");
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

    public PairList nextArguments(Context parentContext, ListVector extraArgs) {


      /*
       * Now update the original arguments with any new values from the previous generic.
       * in the chain. To do this, we have to match the original arguments to the
       * formal names of the previous generic.
       */

      PairList actuals = parentContext.getArguments();
      Closure closure = (Closure) parentContext.getFunction();
      PairList formals = closure.getFormals();
      Environment previousEnv = parentContext.getEnvironment();

      return updateArguments(parentContext, actuals, formals, previousEnv, extraArgs);
    }

    /**
     * For calls via NextMethod, find the original call to the function which calls NextMethod.
     */
    private Context findOriginalCallingContext(Context callContext) {

      Context parentContext = callContext.getParent();
      while(parentContext.getParent() != resolver.previousContext) {
        parentContext = parentContext.getParent();
      }
      return parentContext;
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

  public static SEXP[] initDispatchTable(Environment definitionEnvironment, SEXP generic, SEXP group, SEXP classes) {
    SEXP[] table = new SEXP[DISPATCH_TABLE_SIZE];
    table[GENERIC_DISPATCH_TABLE_INDEX] = generic;
    table[CLASS_DISPATCH_TABLE_INDEX] = classes;
    table[GROUP_DISPATCH_TABLE_INDEX] = group;
    table[GENERIC_DEF_ENV_TABLE_INDEX] = definitionEnvironment;
    return table;
  }

  public static Function findMethod(Context context,
                                    Environment definitionEnvironment,
                                    Environment callingEnvironment,
                                    String genericMethodName,
                                    String group,
                                    Iterable<String> classes,
                                    SEXP[] dispatchTable) {

    Environment methodTable = findMethodTable(context, definitionEnvironment);
    Function method;

    for(String className : classes) {

      method = findMethod(context, methodTable, callingEnvironment, genericMethodName, className, dispatchTable);
      if(method != null) {
        return method;
      }
      if(group != null) {
        method = findMethod(context, methodTable, callingEnvironment, group, className, dispatchTable);
        if(method != null) {
          return method;
        }
      }
    }

    //---this is from nextOrDefault() //

    // Look up the .default method first in the definition environment
    Function function = findMethod(context, methodTable, definitionEnvironment, genericMethodName, "default", dispatchTable);
    if(function != null) {
      return function;
    }

    // Otherwise see if *another* package has defined a default method
    function = findMethod(context, methodTable, callingEnvironment, genericMethodName, "default", dispatchTable);
    if(function != null) {
      dispatchTable[CLASS_DISPATCH_TABLE_INDEX] = Null.INSTANCE;
      return function;
    }

    // as a last step, we call BACK into the primitive
    // to get the default implementation  - ~ YECK ~
    PrimitiveFunction primitive = Primitives.getBuiltin(genericMethodName);
    if(primitive != null) {

      dispatchTable[METHOD_DISPATCH_TABLE_INDEX] = StringVector.valueOf(genericMethodName + ".default");

      return primitive;
    }

    return null;
  }

  private static Function findMethod(Context context,
                                     Environment methodTable,
                                     Environment callingEnvironment,
                                     String name,
                                     String className,
                                     SEXP[] dispatchTable) {

    String method = name + "." + className;
    Symbol methodSymbol = Symbol.get(method);
    Function function = callingEnvironment.findFunction(context, methodSymbol);
    if(function != null) {
      dispatchTable[METHOD_DISPATCH_TABLE_INDEX] = methodSymbol;
      return function;

    } else if(methodTable.hasVariable(methodSymbol)) {
      dispatchTable[METHOD_DISPATCH_TABLE_INDEX] = methodSymbol;
      return (Function) methodTable.getVariableUnsafe(methodSymbol).force(context);

    } else {
      return null;
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
}
