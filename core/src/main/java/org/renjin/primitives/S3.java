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

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.eval.*;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.packaging.SerializedPromise;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.repackaged.guava.collect.*;
import org.renjin.repackaged.guava.primitives.Ints;
import org.renjin.sexp.*;
import org.renjin.sexp.Vector;

import java.util.*;

import static org.renjin.repackaged.guava.collect.Collections2.filter;

/**
 * Primitives used in the implementation of the S3 object system
 */
public class S3 {

  public static final Symbol METHODS_TABLE = Symbol.get(".__S3MethodsTable__.");

  public static final Set<String> GROUPS = Sets.newHashSet("Ops", "Math", "Summary");

  private static final Set<String> ARITH_GROUP = Sets.newHashSet("+", "-", "*", "^", "%%", "%/%", "/");
  
  private static final Set<String> COMPARE_GROUP = Sets.newHashSet("==", ">", "<", "!=", "<=", ">=");
  
  private static final Set<String> LOGIC_GROUP = Sets.newHashSet("&", "&&", "|", "||", "xor");
  
  private static final Set<String> SPECIAL = Sets.newHashSet("$", "$<-");
  
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
   * Attempts to compute the classes used for S3 dispatch based on value bounds for an expression.
   * 
   * @param valueBounds
   * @return a StringVector containing the known class list for this expression, or {@code null} if they could not
   * be deduced.
   */
  public static StringVector computeDataClasses(ValueBounds valueBounds) {
    // If we don't know what the value's class attribute is, we can't make
    // any further assumptions
    if(!valueBounds.isClassAttributeConstant()) {
      return null;
    }

    AtomicVector classAttribute = valueBounds.getConstantClassAttribute();
    if(classAttribute.length() > 0) {
      // S3 class has been explicitly defined and is constant at compile time
      return (StringVector) classAttribute;
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

    int dimCount = valueBounds.getConstantDimCount();
    if(dimCount == 2) {
      dataClass.add("matrix");
    } else if(dimCount > 0) {
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
    if(Types.isS4(args.getElementAsSEXP(0))) {
      return handleS4object(context, args.getElementAsSEXP(0), args, rho, group, opName);
    }
    
    left = Resolver.start(context, group, opName, args.getElementAsSEXP(0))
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
    return left.doApply(context, rho, promisedArgs);
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
      resultS4Dispatch = handleS4object(context, object, args, rho, null, name);
    }
    if (resultS4Dispatch != null) {
      return resultS4Dispatch;
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

  private static boolean isS4DispatchSupported(String name) {
    return !("@<-".equals(name));
  }
  
  private static SEXP handleS4object(@Current Context context, SEXP source, PairList args,
                                     Environment rho, String group, String opName) {
    
    boolean hasS3Class = source.getAttribute(Symbol.get(".S3Class")).length() != 0;
    
    
    List<Environment> groupMethodEnv = null;
    List<Environment> genericMethodEnv = null;
    
    genericMethodEnv = findMethodEnvironment(context, opName);
    if("Ops".equals(group)) {
      groupMethodEnv = findOpsMethodEnvironment(context, opName);
    } else if(!"".equals(group)) {
      groupMethodEnv = findMethodEnvironment(context, group);
    }
    
    if((groupMethodEnv == null || groupMethodEnv.size() == 0) && (genericMethodEnv == null || genericMethodEnv.size() == 0)) {
      return null;
    }
    
    Map<String, List<Environment>> mapMethodEnvList = new HashMap<>();
    if(genericMethodEnv != null && genericMethodEnv.size() != 0) {
      mapMethodEnvList.put("generic", genericMethodEnv);
    }
    if(groupMethodEnv != null && groupMethodEnv.size() != 0) {
      mapMethodEnvList.put("group", groupMethodEnv);
    }
    
    // S4 methods for each generic function is stored in an environment. methods for each signature is stored
    // separately using the signature as name. for example
    // setMethod("[", signature("AA","BB","CC"), function(x, i, j, ...))
    // is stored as `AA#BB#CC` in an environment named `.__T__[:base`
    // here we get the first method from the method environment and split the name by # to know what the expected
    // signature length is. This might be longer the length of arguments and #ANY should be used for missing
    // arguments. In case signature is shorted than the number of arguments we don't need to evaluate the extra
    // arguments.
    
    int[] signatureLength = computeSignatureLength(genericMethodEnv, groupMethodEnv);
    
    int maxSignatureLength = 0;
    for(int i = 0; i < signatureLength.length; i++) {
      maxSignatureLength = signatureLength[i] < maxSignatureLength ? maxSignatureLength : signatureLength[i];
    }
    if(maxSignatureLength == 0) {
      return null;
    }
    
    PairList.Builder rePromisedArgs = new PairList.Builder();
    Iterator<PairList.Node> it = args.nodes().iterator();
    int argIdx = 0;
    while(it.hasNext()) {
      PairList.Node node = it.next();
      SEXP uneval = node.getValue();
      if(argIdx == 0) {
        rePromisedArgs.add(node.getRawTag(), new Promise(uneval, source));
      } else if(argIdx < maxSignatureLength) {
        SEXP evaled = context.evaluate(uneval, rho);
        rePromisedArgs.add(node.getRawTag(), new Promise(uneval, evaled));
      } else {
        rePromisedArgs.add(node.getRawTag(), new Promise(rho, uneval));
      }
      argIdx++;
    }
  
    Map<String, List<List<MethodRanking>>> possibleSignatures = generateSignatures(context, rho, mapMethodEnvList, rePromisedArgs.build(), signatureLength);
  
    List<List<SelectedMethod>> selectedMethods = findMatchingMethods(context, mapMethodEnvList, possibleSignatures);
    
    if(selectedMethods.size() == 0) {
      return null;
    }
    
    int maxNumberOfMethods = 0;
    for(int i = 0; i < selectedMethods.size(); i++) {
      if(selectedMethods.get(i).size() > maxNumberOfMethods) {
        maxNumberOfMethods = selectedMethods.get(i).size();
      }
    }
    if(maxNumberOfMethods == 0) {
      return null;
    }
  
    SelectedMethod method = null;
    if(selectedMethods.get(0).size() == 0) {
      method = selectedMethods.get(1).get(0);
    } else {
      method = selectedMethods.get(0).get(0);
    }
    
//     if selected method is from Group or if its from standard generic but distance is > 0
//     returned function environment is populated with the following metadata objects:
//     - .defined:  signature method
//     - .target:   signature target
//     - .Generic:
//     - .Method:   method definition
//     - .Methods:  function call
//     - e1:        arg1
//     - e2:        arg2
//
//     otherwise only e1 and e2.
    
    if (("generic".equals(method.getGroup()) && method.getDistance() == 0) || hasS3Class) {
      return context.evaluate(new FunctionCall(method.getFunction(), rePromisedArgs.build()));
    } else {
      SEXP variableDotDefined = buildDotTargetOrDefined(context, method, true);
      SEXP variableDotTarget = buildDotTargetOrDefined(context, method, false);
      SEXP variableDotGeneric = buildDotGeneric(opName);
      
      Map<Symbol, SEXP> metadata = new HashMap<>();
      metadata.put(Symbol.get(".defined"), variableDotDefined);
      metadata.put(Symbol.get(".Generic"), variableDotGeneric);
      metadata.put(Symbol.get(".Method"), method.getFunction());
      metadata.put(Symbol.get(".Methods"), Symbol.get(".Primitive(\"" + opName +"\")"));
      metadata.put(Symbol.get(".target"), variableDotTarget);
      
      FunctionCall call = new FunctionCall(method.getFunction(), args);
      Closure closure = method.getFunction();
      return ClosureDispatcher.apply(context,rho, call, closure, rePromisedArgs.build(), metadata);
    }
  }
  
  private static SEXP buildDotGeneric(String opName) {
    SEXP generic = StringVector.valueOf(opName);
    generic.setAttribute("package", StringVector.valueOf("base"));
    return generic;
  }
  
  /**
   * Current GNU R (v3.3.1) seems to set the package-attribute in '.target' to
   * 'methods' for all the arguments (independent of input and method signature).
   *
   * For '.defined', the class of selected method formals are used. 'methods' is
   * used for the selected method arguments of class "ANY" or atomic vectors.
   *
   * > setClass("A", representation(a="numeric"))
   * > setMethod("[", signature("A","A","ANY"), function(x,i,j,...) environment())
   * > x <- a[a,1]
   * > cat(deparse( attr(x$.target, "package") ))
   * c("methods", "methods","methods")
   * > cat(deparse( attr(x$.defined, "package") ))
   * c("methods", ".GlobalEnv","methods")
   * */
  private static SEXP buildDotTargetOrDefined(Context context, SelectedMethod method, boolean defined) {
    
    List<String> argumentClasses;
    argumentClasses = Arrays.asList(method.getSignature().split("#"));
    
    List<String> argumentPackages = new ArrayList<>();
    
    if(defined) {
      for (String argumentClass : argumentClasses) {
        argumentPackages.add(findClassPackage(context, argumentClass));
      }
    } else {
      for (String ignored : argumentClasses) {
        argumentPackages.add("methods");
      }
    }
    
    return new StringVector.Builder()
      .addAll(argumentClasses)
      .setAttribute("names", method.getFunction().getFormals().getNames())
      .setAttribute("package", new StringArrayVector(argumentPackages))
      .setAttribute("class", classWithPackage("signature", "methods"))
      .build();
  }
  
  private static SEXP classWithPackage(String className, String packageName) {
    return StringVector.valueOf(className)
      .setAttribute("package", StringVector.valueOf(packageName));
  }
  
  private static String findClassPackage(Context context, String className) {
    Environment environment = context.getGlobalEnvironment();
    SEXP classS4Object = environment.findVariable(context, Symbol.get(".__C__" + className));
    if (classS4Object instanceof SerializedPromise || "ANY".equals(className)) {
      return "methods";
    }
    return classS4Object.getAttribute(Symbol.get("package")).asString();
  }
  
  private static List<Environment> findMethodEnvironment(Context context, String opName) {
    Symbol methodSymbol = Symbol.get(".__T__" + opName + ":base");
    SEXP genericMethodSymbol;
    List<Environment> methodList = new ArrayList<>();
  
    if (SPECIAL.contains(opName)) {
      Namespace methodsNamespace = context.getNamespaceRegistry().getNamespace(context, "methods");
      Frame methodFrame = methodsNamespace.getNamespaceEnvironment().getFrame();
      genericMethodSymbol = methodFrame.getVariable(methodSymbol).force(context);
      if(genericMethodSymbol == Symbol.UNBOUND_VALUE || !(genericMethodSymbol instanceof Environment)) {
        return null;
      }
      methodList.add((Environment) genericMethodSymbol);
    } else {
      
      SEXP globalMethodEnv = context.getGlobalEnvironment().getFrame().getVariable(methodSymbol);
      if (globalMethodEnv != Symbol.UNBOUND_VALUE && globalMethodEnv instanceof Environment) {
        methodList.add((Environment) globalMethodEnv);
      }
      
      for(Symbol loadedNamespace : context.getNamespaceRegistry().getLoadedNamespaces()) {
        String packageName = loadedNamespace.getPrintName();
        Namespace packageNamespace = context.getNamespaceRegistry().getNamespace(context, packageName);
        Environment packageEnvironment = packageNamespace.getNamespaceEnvironment();
        SEXP packageMethodEnv = packageEnvironment.getFrame().getVariable(methodSymbol).force(context);
        if(packageMethodEnv instanceof Environment) {
          methodList.add((Environment) packageMethodEnv);
        }
      }
    }
    return methodList.size() == 0 ? null : methodList;
  }
  
  private static List<Environment> findOpsMethodEnvironment(Context context, String opName) {
    List<Environment> methodList = new ArrayList<>();
    
    Frame globalFrame = context.getGlobalEnvironment().getFrame();
    SEXP globalMethodEnv = getMethodEnvironment(context, opName, globalFrame);
    if (globalMethodEnv != null && globalMethodEnv instanceof Environment) {
      methodList.add((Environment) globalMethodEnv);
    }
  
    for(Symbol packageSymbol : context.getNamespaceRegistry().getLoadedNamespaces()) {
      String packageName = packageSymbol.getPrintName();
      Collection<Symbol> exports = context.getNamespaceRegistry().getNamespace(context, packageName).getExports();
      if(exports.contains(Symbol.get("Arith")) ||
          exports.contains(Symbol.get("Compare")) ||
          exports.contains(Symbol.get("Logic")) ||
          exports.contains(Symbol.get(opName))) {
        Namespace packageNamespace = context.getNamespaceRegistry().getNamespace(context, packageName);
        Frame packageFrame = packageNamespace.getNamespaceEnvironment().getFrame();
        SEXP packageMethodEnvironment = getMethodEnvironment(context, opName, packageFrame);
        if(packageMethodEnvironment instanceof Environment &&
            ((Environment) packageMethodEnvironment).getFrame().getSymbols().size() > 0) {
          methodList.add((Environment) packageMethodEnvironment);
        }
      }
    }
    
    if (methodList.size() == 0) {
      return null;
    }
    
    return methodList;
  }
  
  private static SEXP getMethodEnvironment(Context context, String opName, Frame packageFrame) {
    SEXP packageMethodEnv = null;
    if(ARITH_GROUP.contains(opName)) {
      String[] groups = {".__T__Arith:base", ".__T__Ops:base"};
      packageMethodEnv = getEnvironment(context, packageFrame, groups);
    } else if (COMPARE_GROUP.contains(opName)) {
      String[] groups = {".__T__Compare:methods", ".__T__Ops:base"};
      packageMethodEnv = getEnvironment(context, packageFrame, groups);
    } else if (LOGIC_GROUP.contains(opName)) {
      String[] groups = {".__T__Logic:base", ".__T__Ops:base"};
      packageMethodEnv = getEnvironment(context, packageFrame, groups);
    }
    return packageMethodEnv;
  }
  
  private static SEXP getEnvironment(Context context, Frame frame, String[] groups) {
    SEXP method = null;
    for (int i = 0; i < groups.length && method == null; i++) {
      SEXP foundMethodEnvironment = frame.getVariable(Symbol.get(groups[i])).force(context);
      method = foundMethodEnvironment instanceof Environment ? (Environment) foundMethodEnvironment : null;
    }
    return method;
  }
  
  private static int[] computeSignatureLength(List<Environment> genericEnvs, List<Environment> groupEnvs) {
    List<Environment> methodEnvironment = genericEnvs == null ? groupEnvs : genericEnvs;
    int[] length = new int[methodEnvironment.size()];
    for(int i = 0; i < methodEnvironment.size(); i++) {
      if(methodEnvironment.get(i).getFrame().getSymbols().iterator().hasNext()) {
        length[i] = methodEnvironment.get(i).getFrame().getSymbols().iterator().next().getPrintName().split("#").length;
      } else {
        length[i] = 0;
      }
    }
    return length;
  }
  
  
  /**
   *
   * This function loops through an ArrayList of generated MethodRankings and stores the signatures for
   * which a method can be found, together with the method and the accompanying meta data from MethodRanking
   * class in an new ArrayList<SelectedMethod>. The ArrayList<MethodRanking> input should be sorted before
   * calling this function.
   *
   * The selected method, method signature, total distance, and method type (generic or group) information
   * are stored in SelectedMethod class. This is necessary to be able to give generic methods priority over
   * group methods, and also for giving a warning for ignored valid methods.
   *
   *
   * */
  private static List<List<SelectedMethod>> findMatchingMethods(Context context, Map<String, List<Environment>> mapMethodEnvList,
                                                          Map<String, List<List<MethodRanking>>> mapSignatureList) {
    
    List<List<SelectedMethod>> listMethods = new ArrayList<>();
    
    for(int e = 0; e < mapSignatureList.size(); e++) {
      String type = mapSignatureList.keySet().toArray(new String[0])[e];
      List<List<MethodRanking>> rankings = mapSignatureList.get(type);
      List<Environment> listMethodEnv = mapMethodEnvList.get(type);
      
      for(int i = 0; i < rankings.size(); i++) {
        List<MethodRanking> rankedMethodsList = rankings.get(i);
        List<SelectedMethod> selectedMethods = new ArrayList<>();
        String inputSignature = rankedMethodsList.get(0).getSignature();
    
        for (MethodRanking rankedMethod : rankedMethodsList) {
          String signature = rankedMethod.getSignature();
          int distance = rankedMethod.getTotalDist();
          Symbol signatureSymbol = Symbol.get(signature);
          SEXP function = listMethodEnv.get(i).getFrame().getVariable(signatureSymbol).force(context);
      
          if (function instanceof Closure) {
            selectedMethods.add(new SelectedMethod((Closure) function, type, distance, signature, signatureSymbol, inputSignature));
          }
        }
        listMethods.add(selectedMethods);
      }
    }
    
    return listMethods;
  }
  
  /**
   * Provided a PairList of arguments and the number of arguments used for signature
   * this function first evaluates the arguments and extracts their class and superclass
   * information.
   *
   * */
  private static Map<String, List<List<MethodRanking>>> generateSignatures(Context context, Environment rho,
                                                                           Map<String, List<Environment>> mapEnvironmentLists,
                                                                           PairList inputArgs, int[] depth) {
    
    Map<String, List<List<MethodRanking>>> mapListMethods = new HashMap<>();
    
    for(int e = 0; e < mapEnvironmentLists.size(); e++) {
      String type = mapEnvironmentLists.keySet().toArray(new String[0])[e];
      List<Environment> functionEnvs = mapEnvironmentLists.get(type);
      List<List<MethodRanking>> listSignatures = new ArrayList<>();
  
  
      for(int listIdx = 0; listIdx < functionEnvs.size(); listIdx++) {
        Environment functionEnv = functionEnvs.get(listIdx);
    
        ArgumentSignature[] argSignatures = new ArgumentSignature[depth[listIdx]];
        Symbol methodSymbol = (Symbol) functionEnv.getFrame().getSymbols().toArray()[0];
        Closure genericClosure = (Closure) functionEnv.getFrame().getVariable(methodSymbol);
        PairList formals = genericClosure.getFormals();
        PairList matched = S3.matchArguments(formals, inputArgs, true);

        Iterator<PairList.Node> itMt = matched.nodes().iterator();
        List<Symbol> matchSym = new ArrayList<>();
        while (itMt.hasNext()) {
          matchSym.add(itMt.next().getTag());
        }
        int dotIdx = matchSym.indexOf(Symbols.ELLIPSES);

        Iterator<PairList.Node> it = inputArgs.nodes().iterator();
        Iterator<PairList.Node> itM = matched.nodes().iterator();
        int idx = 0;
        while(it.hasNext() && idx < depth[listIdx]) {
          PairList.Node node = it.next();
          if(!node.hasTag() || (node.hasTag() && node.getTag() != Symbols.ELLIPSES)) {
            SEXP nodeValue = node.getValue();
            if(nodeValue == Symbol.MISSING_ARG || nodeValue.force(context) == Symbol.MISSING_ARG) {
              argSignatures[idx] = new ArgumentSignature();
            } else {
              if(node.hasTag() && matchSym.indexOf(node.getTag()) > dotIdx && idx < dotIdx) {
                argSignatures[idx] = new ArgumentSignature();
                idx++;
              }
              String[] nodeClass;
              String[] testClass = computeDataClasses(context, nodeValue).toArray();
              if(testClass.length == 1 && nodeValue.force(context) instanceof StringArrayVector && "signature".equals(testClass[0])) {
                nodeClass = ((StringArrayVector)nodeValue.force(context)).toArray();
              } else {
                nodeClass = computeDataClasses(context, nodeValue).toArray();
              }
              ArgumentSignature argSig = getClassAndDistance(context, nodeClass);
              argSignatures[idx] = argSig;
            }
            idx++;
          }
        }
        
        for(int i = 0; i < argSignatures.length; i++) {
          if(argSignatures[i] == null) {
            argSignatures[i] = new ArgumentSignature(new String[]{"missing", "ANY"}, new int[]{0, 1} );
          }
        }
    
        int numberOfPossibleSignatures = 1;
        for(int i = 0; i < argSignatures.length; i++) {
          numberOfPossibleSignatures = numberOfPossibleSignatures * argSignatures[i].getArgument().length;
        }
    
        List<MethodRanking> possibleSignatures = new ArrayList<>(numberOfPossibleSignatures);
    
        int numberOfClassesCurrentArgument;
        int argumentClassIdx = 0;
        int repeat = 1;
        int repeatIdx = 1;
    
        for(int col = 0; col < depth[listIdx]; col++) {
          numberOfClassesCurrentArgument = argSignatures[col].getArgument().length;
          for(int row = 0; row < numberOfPossibleSignatures; row++, repeatIdx++) {
            if(argumentClassIdx == numberOfClassesCurrentArgument) {
              argumentClassIdx = 0;
            }
            ArgumentSignature argSignature = argSignatures[col];
            String signature = argSignature.getArgument(argumentClassIdx);
            if(possibleSignatures.isEmpty() ||
                possibleSignatures.size() < row + 1 ||
                possibleSignatures.get(row) == null) {
              int[] distance = argSignature.getDistanceAsArray(argumentClassIdx);
              possibleSignatures.add(row, new MethodRanking(signature, distance));
            } else {
              int distance = argSignature.getDistance(argumentClassIdx);
              possibleSignatures.set(row, possibleSignatures.get(row).append(signature, distance));
            }
            if(repeat == 1) {
              argumentClassIdx++;
            }
            if(repeat != 1 && repeatIdx == repeat) {
              repeatIdx = 0;
              argumentClassIdx++;
            }
          }
          repeatIdx = 1;
          argumentClassIdx = 0;
          repeat = repeat * numberOfClassesCurrentArgument;
        }
        Collections.sort(possibleSignatures);
        listSignatures.add(possibleSignatures);
      }
  
      mapListMethods.put(type, listSignatures);
    }
    
    return mapListMethods;
  }
  
  /**
   * Each class is stored as an S4 object with prefix '.__C__'. This S4 object
   * has a attribute 'contains' where the superclass names and distance are stored.
   * The first class is the input class with distance of 0. All of the super classes
   * and their distance are added using the names of 'contains' and the accompanying
   * value in 'distance' slot. "ANY" is added as the last class and the distance is
   * defined as the maximum distance + 1. The class names and distances are stored in
   * an ArgumentSignature class and return.
   *
   * */
  
  private static ArgumentSignature getClassAndDistance(Context context, String[] argClass) {

    List<Integer> distances = new ArrayList<>();
    List<String> classes = new ArrayList<>();
    for(int i = 0; i < argClass.length; i++) {
      classes.add(argClass[i]);
      distances.add(0);
    }
    Symbol argClassObjectName = Symbol.get(".__C__" + argClass[0]);
    Frame globalFrame = context.getGlobalEnvironment().getFrame();
    AttributeMap map = globalFrame.getVariable(argClassObjectName).getAttributes();
    SEXP containsSlot = map.get("contains");
    SEXP argSuperClasses = containsSlot.getNames();

    for(int i = 0; i < argSuperClasses.length(); i++) {
      SEXP distanceSlot = ((ListVector) containsSlot).get(i).getAttributes().get("distance");
      distances.add(((Vector) distanceSlot).getElementAsInt(0));
      classes.add(((Vector) argSuperClasses).getElementAsString(i));
    }

    int max = Collections.max(distances);
    if(!classes.contains("ANY") && !classes.contains("NULL")) {
      distances.add(max + 1);
      classes.add("ANY");
    }

    return new ArgumentSignature(classes.toArray(new String[0]), Ints.toArray(distances));
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
  
  private static <X> X first(Iterable<X> values) {
    return values.iterator().next();
  }
  
  private static boolean hasNextUnTagged(PeekingIterator<PairList.Node> it) {
    return it.hasNext() && !it.peek().hasTag();
  }
  
  private static PairList.Node nextUnTagged(Iterator<PairList.Node> it) {
    PairList.Node arg = it.next() ;
    while( arg.hasTag() ) {
      arg = it.next();
    }
    return arg;
  }
  
  private static PairList matchArguments(PairList formals, PairList actuals, boolean populateMissing) {
    
    List<PairList.Node> unmatchedActuals = Lists.newArrayList();
    for(PairList.Node argNode : actuals.nodes()) {
      unmatchedActuals.add(argNode);
    }
    
    List<PairList.Node> unmatchedFormals = Lists.newArrayList(formals.nodes());
    List<SEXP> formalTags = new ArrayList<>();
    Symbol[] tags = new Symbol[unmatchedFormals.size()];
    SEXP[] values = new SEXP[unmatchedFormals.size()];
    
    // do exact matching
    int argIdx = 0;
    for(ListIterator<PairList.Node> formalIt = unmatchedFormals.listIterator(); formalIt.hasNext(); ) {
      PairList.Node formal = formalIt.next();
      formalTags.add(formal.getTag());
      if(formal.hasTag()) {
        Symbol name = formal.getTag();
        if(name != Symbols.ELLIPSES) {
          Collection<PairList.Node> matches = Collections2.filter(unmatchedActuals, PairList.Predicates.matches(name));
          if (matches.size() == 1) {
            PairList.Node match = first(matches);
            SEXP value = match.getValue();
            tags[argIdx] = name;
            values[argIdx] = value;
            formalIt.remove();
            unmatchedActuals.remove(match);
            
          } else if (matches.size() > 1) {
            throw new EvalException(String.format("Multiple named values provided for argument '%s'", name.getPrintName()));
          }
        }
      }
      argIdx++;
    }
    
    // Partial matching
    Collection<PairList.Node> remainingNamedFormals = filter(unmatchedFormals, PairList.Predicates.hasTag());
    for (Iterator<PairList.Node> actualIt = unmatchedActuals.iterator(); actualIt.hasNext(); ) {
      PairList.Node actual = actualIt.next();
      int formalIdx = 0;
      if (actual.hasTag() && actual.getTag() != Symbols.ELLIPSES) {
        String argumentName = actual.getTag().getPrintName();
        PairList.Node partialMatch = null;
        for (PairList.Node formal : remainingNamedFormals) {
          formalIdx = formalTags.indexOf(formal.getTag());
          if(formal.getTag() == Symbols.ELLIPSES) {
            break;
          }
          if(formal.getTag().getPrintName().startsWith(argumentName)) {
            if(partialMatch == null) {
              partialMatch = formal;
            } else {
              throw new EvalException(String.format("Provided argument '%s' matches multiple named formal arguments", argumentName));
            }
          }
        }
        
        if (partialMatch != null) {
          tags[formalIdx] = partialMatch.getTag();
          values[formalIdx] = actual.getValue();
          actualIt.remove();
          unmatchedFormals.remove(partialMatch);
        }
      }
      formalIdx++;
    }
    
    // match any unnamed args positionally
    
    Iterator<PairList.Node> formalIt = unmatchedFormals.iterator();
    PeekingIterator<PairList.Node> actualIt = Iterators.peekingIterator(unmatchedActuals.iterator());
    for (int i = 0; i < values.length; i++) {
      if(values[i] == null) {
        if( formalIt.hasNext()) {
          PairList.Node formal = formalIt.next();
          if(Symbols.ELLIPSES.equals(formal.getTag())) {
            PromisePairList.Builder promises = new PromisePairList.Builder();
            while(actualIt.hasNext()) {
              PairList.Node actual = actualIt.next();
              promises.add( actual.getRawTag(),  actual.getValue() );
            }
            tags[i] = formal.getTag();
            values[i] = promises.build();
            
          } else if( hasNextUnTagged(actualIt) ) {
            PairList.Node nextUnTagged = nextUnTagged(actualIt);
            tags[i] = formal.getTag();
            values[i] = nextUnTagged.getValue();
            
          } else if(populateMissing) {
            tags[i] = formal.getTag();
            values[i] = Symbol.MISSING_ARG;
          }
        }
      }
    }
    if(actualIt.hasNext()) {
      throw new EvalException("Unmatched positional arguments");
    }
    PairList.Builder builder = new PairList.Builder();
    for (int i = 0; i < values.length; i++) {
      if(values[i] != null) {
        builder.add(tags[i], values[i]);
      }
    }
    PairList matchedArgs = builder.build();
    return matchedArgs;
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
  
  public static class ArgumentSignature {
    private String[] argumentClasses;
    private int[] distances;
    
    ArgumentSignature(String[] classes, int[] distances) {
      this.argumentClasses = classes;
      this.distances = distances;
    }
    
    ArgumentSignature() {
      this.argumentClasses = new String[]{"missing", "ANY"};
      this.distances = new int[]{0, 1};
    }
    
    public String[] getArgument() {
      return argumentClasses;
    }
    
    public String getArgument(int position) {
      return this.argumentClasses[position];
    }
    
    public int getDistance(int position) {
      return this.distances[position];
    }
    
    public int[] getDistanceAsArray(int position) {
      int[] array = new int[1];
      array[0] = this.distances[position];
      return array;
    }
  }
  
  public static class MethodRanking implements Comparable <MethodRanking> {
    private String signature;
    private int[] distances;
    private boolean has0 = false;
    private int totalDist = 0;
    private double rank = 0.0;
    
    MethodRanking(String signature, int[] distance) {
      this.signature = signature;
      this.distances = distance;
      
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
  
    @Override
    public String toString() {
      return "MethodRanking{" +
        "signature='" + signature + '\'' +
        '}';
    }
  
    public MethodRanking append(String argument, int distance) {
      String newSig = this.signature + "#" + argument;
      int[] newDist = new int[this.distances.length + 1];
      for(int i = 0; i < this.distances.length; i++) {
        newDist[i] = this.distances[i];
      }
      newDist[this.distances.length] = distance;
      this.signature = newSig;
      this.distances = newDist;
      this.has0 = (this.has0 == true || distance == 0);
      this.totalDist = this.totalDist + distance;
      this.rank = this.rank + ((10007.0 * Math.pow(0.5, (double) newDist.length)) * (double) distance);
      return this;
    }
    
    public String getSignature() {
      return signature;
    }
    
    public int[] getDistance() {
      return distances;
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
    public int compareTo(MethodRanking o) {
      int i = Integer.compare(this.getTotalDist(), o.getTotalDist());
      if (i != 0) {
        return i;
      }
      i = Integer.compare(this.isHas0(), o.isHas0());
      if (i != 0) {
        return i;
      }
      return Double.compare(rank, o.getRank());
    }
  }
  
  public static class SelectedMethod {
    private Closure function;
    private String group;
    private int currentDistance;
    private String currentSig;
    private Symbol methodName;
    private String methodInputSignature;
    
    SelectedMethod(Closure fun, String grp, int dist, String sig, Symbol method, String methSig) {
      this.function = fun;
      this.group = grp;
      this.currentDistance = dist;
      this.currentSig = sig;
      this.methodName = method;
      this.methodInputSignature = methSig;
    }
    
    public Closure getFunction() {
      return function;
    }
    
    public String getGroup() {
      return group;
    }
    
    public int getDistance() {
      return currentDistance;
    }
    
    public String getSignature() {
      return currentSig;
    }
    
    public Symbol getMethod() {
      return methodName;
    }
    
    public String getInputSignature() {
      return methodInputSignature;
    }
  }
}
