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
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.PeekingIterator;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.repackaged.guava.primitives.Ints;
import org.renjin.sexp.*;
import org.renjin.sexp.Vector;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

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

  private static final Symbol NA_RM = Symbol.get("na.rm");

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
    
    
    List<Environment> groupMethodTables = null;
    List<Environment> genericMethodTables = null;
    
    genericMethodTables = findMethodTable(context, opName);
    if("Ops".equals(group)) {
      groupMethodTables = findOpsMethodTable(context, opName);
    } else if(!("".equals(group) || group == null)) {
      groupMethodTables = findMethodTable(context, group);
    }
    
    if((groupMethodTables == null || groupMethodTables.size() == 0) && (genericMethodTables == null || genericMethodTables.size() == 0)) {
      return null;
    }
    
    Map<String, List<Environment>> mapMethodTableList = new HashMap<>();
    if(genericMethodTables != null && genericMethodTables.size() != 0) {
      mapMethodTableList.put("generic", genericMethodTables);
    }
    if(groupMethodTables != null && groupMethodTables.size() != 0) {
      mapMethodTableList.put("group", groupMethodTables);
    }
    
    // S4 methods for each generic function is stored in method table of type environment. methods for each signature is stored
    // separately using the signature as name. for example
    // setMethod("[", signature("AA","BB","CC"), function(x, i, j, ...))
    // is stored as `AA#BB#CC` in an environment named `.__T__[:base` (we call this the methodCache)
    // here we get the first method from the method table and split the name by # to know what the expected
    // signature length is. This might be longer the length of arguments and #ANY should be used for missing
    // arguments. "ANY" should not be used for arguments which are explicitely named as "missing" or "NULL".
    // In case signature is shorter than the number of arguments we don't need to evaluate the extra
    // arguments. Since each package can contain a method table for the same function but different signature
    // lengths the return of computeSignatureLength is an integer array with the length of signature for
    // each found method table.
    
    Map<String, int[]> signatureLength = computeSignatureLength(genericMethodTables, groupMethodTables);
    
    int maxSignatureLength = getMaxSignatureLength(signatureLength);
    
    if(maxSignatureLength == 0) {
      return null;
    }
    
    // expand ... in arguments or remove if empty
    PairList expandedArgs = Calls.promiseArgs(args, context, rho);
    PairList.Builder promisedArgs = new PairList.Builder();
    Iterator<PairList.Node> it = expandedArgs.nodes().iterator();
    int argIdx = 0;
    while(it.hasNext()) {
      PairList.Node node = it.next();
      SEXP uneval = node.getValue();
      if(argIdx == 0) {
        // The source has already been evaluated to check for class
        promisedArgs.add(node.getRawTag(), new Promise(uneval, source));
      } else {
        // otherwise a promise is created to evaluate if necessary
        if(uneval == Symbol.MISSING_ARG) {
          promisedArgs.add(node.getRawTag(), uneval);
        } else {
          promisedArgs.add(node.getRawTag(), Promise.repromise(rho, uneval));
        }
      }
      argIdx++;
    }
  
    // Based on the signature length in each method table and the class of the input arguments, signatures and
    // distances (stored as MethodRanking class) are generated for each method table seperately. The method tables
    // and resulting signatures for generic and group functions are kept seperately in HashMap with keys "generic" or
    // "group". This is necessary since "generic" methods are prioritized over "group" methods when distance is same.
    
    Map<String, List<List<MethodRanking>>> possibleSignatures = generateSignatures(context, mapMethodTableList, promisedArgs.build(), signatureLength);
  
    // The generated signatures are sorted based on their distance to input signature and looked up in originating
    // method table. All signatures are looked up and the ones present are returned.
    
    Map<String, List<SelectedMethod>> validMethods = findMatchingMethods(context, mapMethodTableList, possibleSignatures);
    
    if(validMethods.keySet().size() == 0) {
      return null;
    }
    
    int maxNumberOfMethods = 0;
    Iterator<String> typeItr = validMethods.keySet().iterator();
    for(;typeItr.hasNext();) {
      List<SelectedMethod> methods = validMethods.get(typeItr.next());
      int nextSize = methods.size();
      if (nextSize > maxNumberOfMethods) {
        maxNumberOfMethods = nextSize;
      }
    }
    
    if(maxNumberOfMethods == 0) {
      return null;
    }
  
    SelectedMethod method;
    SelectedMethod genericMethod = null;
    SelectedMethod groupMethod = null;
    double genericRank = -1;
    double groupRank = -1;
    if(validMethods.containsKey("generic")) {
      genericRank = validMethods.get("generic").get(0).getRank();
      genericMethod = validMethods.get("generic").get(0);
    }
    if(validMethods.containsKey("group")) {
      groupRank = validMethods.get("group").get(0).getRank();
      groupMethod = validMethods.get("group").get(0);
    }
  
    if (genericRank == -1 || (groupRank != -1 && genericRank > groupRank)) {
      method = groupMethod;
    } else {
      method = genericMethod;
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
    
    Closure function = method.getFunction();
    
    if (("generic".equals(method.getGroup()) && method.getDistance() == 0) || hasS3Class) {
      SEXP call = new FunctionCall(function, promisedArgs.build());
      return context.evaluate( call );
    } else {
      Map<Symbol, SEXP> metadata = new HashMap<>();
      metadata.put(Symbol.get(".defined"), buildDotTargetOrDefined(context, method, true));
      metadata.put(Symbol.get(".Generic"), buildDotGeneric(opName));
      metadata.put(Symbol.get(".Method"), function);
      metadata.put(Symbol.get(".Methods"), Symbol.get(".Primitive(\"" + opName +"\")"));
      metadata.put(Symbol.get(".target"), buildDotTargetOrDefined(context, method, false));
  
      PairList formals = function.getFormals();
      PairList matchedList = ClosureDispatcher.matchArguments(formals, promisedArgs.build(), true);
      Map<Symbol, SEXP> matchedMap = new HashMap<>();
      for (PairList.Node node : matchedList.nodes()) {
        matchedMap.put(node.getTag(), node.getValue());
      }

      for(Symbol arg : matchedMap.keySet()) {
        SEXP argValue = matchedMap.get(arg);
        if(argValue != Symbol.MISSING_ARG) {
          if(argValue instanceof Promise && ((Promise) argValue).getValue() != null) {
            metadata.put(arg, ((Promise) argValue).getValue());
          } else {
            metadata.put(arg, argValue.force(context));
          }
        }
      }
  
      FunctionCall call = new FunctionCall(function, expandedArgs);
      return ClosureDispatcher.apply(context, rho, call, function, promisedArgs.build(), metadata);
    }
  }
  
  private static int getMaxSignatureLength(Map<String, int[]> signatureLengths) {
    int max = 0;
    String[] types = new String[]{"generic", "group"};
    for(String type : types) {
      if(signatureLengths.containsKey(type)) {
        int currentMax = Ints.max(signatureLengths.get(type));
        max = max < currentMax ? currentMax : max;
      }
    }
    return max;
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
  
  private static List<Environment> findMethodTable(Context context, String opName) {
    String genericName = findStandardGenericName(context, opName);
    Symbol methodSymbol = Symbol.get(genericName);
    SEXP methodTableMethodsPkg;
    List<Environment> methodTableList = new CopyOnWriteArrayList<>();
  
    SEXP methodTableGlobalEnv = context.getGlobalEnvironment().getFrame().getVariable(methodSymbol);
    if (methodTableGlobalEnv != Symbol.UNBOUND_VALUE && methodTableGlobalEnv instanceof Environment) {
      methodTableList.add((Environment) methodTableGlobalEnv);
    }
  
    List<Symbol> loadedPackages = new ArrayList<>();
    for(Symbol symbol : context.getNamespaceRegistry().getLoadedNamespaces()) {
      loadedPackages.add(symbol);
    }
  
    for(Symbol loadedNamespace : loadedPackages) {
      String packageName = loadedNamespace.getPrintName();
      Namespace packageNamespace = context.getNamespaceRegistry().getNamespace(context, packageName);
      Environment packageEnvironment = packageNamespace.getNamespaceEnvironment();
      SEXP methodTablePackage = packageEnvironment.getFrame().getVariable(methodSymbol).force(context);
      if(methodTablePackage instanceof Environment) {
        methodTableList.add((Environment) methodTablePackage);
      }
    }
    
    return methodTableList.size() == 0 ? null : methodTableList;
  }
  
  private static String findStandardGenericName(Context context, String opName) {
    
    String sourcePackage = null;
    
    List<Symbol> loadedPackages = new CopyOnWriteArrayList<>();
    for(Symbol symbol : context.getNamespaceRegistry().getLoadedNamespaces()) {
      loadedPackages.add(symbol);
    }
  
    for(int i = 0; i < loadedPackages.size() && sourcePackage == null; i++) {
      String packageName = loadedPackages.get(i).getPrintName();
      String possibleGeneric = ".__T__" + opName + ":" + packageName;
      Namespace packageNamespace = context.getNamespaceRegistry().getNamespace(context, packageName);
      Environment packageEnvironment = packageNamespace.getNamespaceEnvironment();
      SEXP methodTablePackage = packageEnvironment.getFrame().getVariable(Symbol.get(possibleGeneric)).force(context);
      if(methodTablePackage instanceof Environment) {
        sourcePackage = possibleGeneric;
      }
    }
    
    if(sourcePackage == null) {
      sourcePackage = ".__T__" + opName + ":base";
    }
    return sourcePackage;
  }
  
  private static List<Environment> findOpsMethodTable(Context context, String opName) {
    List<Environment> methodTableList = new ArrayList<>();
    
    Frame globalFrame = context.getGlobalEnvironment().getFrame();
    SEXP methodTableGlobalEnv = getMethodTable(context, opName, globalFrame);
    if (methodTableGlobalEnv instanceof Environment) {
      methodTableList.add((Environment) methodTableGlobalEnv);
    }
  
    for(Symbol packageSymbol : context.getNamespaceRegistry().getLoadedNamespaces()) {
      String packageName = packageSymbol.getPrintName();
      Namespace packageNamespace = context.getNamespaceRegistry().getNamespace(context, packageName);
      Frame packageFrame = packageNamespace.getNamespaceEnvironment().getFrame();
      SEXP methodTablePackage = getMethodTable(context, opName, packageFrame);
      if(methodTablePackage instanceof Environment &&
          ((Environment) methodTablePackage).getFrame().getSymbols().size() > 0) {
        methodTableList.add((Environment) methodTablePackage);
      }
    }
    
    if (methodTableList.size() == 0) {
      return null;
    }
    
    return methodTableList;
  }
  
  private static SEXP getMethodTable(Context context, String opName, Frame packageFrame) {
    SEXP methodTable = null;
    if(ARITH_GROUP.contains(opName)) {
      String[] groups = {".__T__Arith:base", ".__T__Ops:base"};
      methodTable = getMethod(context, packageFrame, groups);
    } else if (COMPARE_GROUP.contains(opName)) {
      String[] groups = {".__T__Compare:methods", ".__T__Ops:base"};
      methodTable = getMethod(context, packageFrame, groups);
    } else if (LOGIC_GROUP.contains(opName)) {
      String[] groups = {".__T__Logic:base", ".__T__Ops:base"};
      methodTable = getMethod(context, packageFrame, groups);
    }
    return methodTable;
  }
  
  private static SEXP getMethod(Context context, Frame frame, String[] groups) {
    SEXP methodTable = null;
    for (int i = 0; i < groups.length && methodTable == null; i++) {
      SEXP foundMethodTable = frame.getVariable(Symbol.get(groups[i])).force(context);
      methodTable = foundMethodTable instanceof Environment ? (Environment) foundMethodTable : null;
    }
    return methodTable;
  }
  
  private static Map<String, int[]> computeSignatureLength(List<Environment> genericMethodTable, List<Environment> groupMethodTable) {
    Map<String, int[]> signatureLengths = new HashMap<>();
    
    if(genericMethodTable != null) {
      int[] length = new int[genericMethodTable.size()];
      for(int i = 0; i < genericMethodTable.size(); i++) {
        if(genericMethodTable.get(i).getFrame().getSymbols().iterator().hasNext()) {
          length[i] = genericMethodTable.get(i).getFrame().getSymbols().iterator().next().getPrintName().split("#").length;
        } else {
          length[i] = 0;
        }
      }
      signatureLengths.put("generic", length);
    }
    
    if(groupMethodTable != null) {
      int[] length = new int[groupMethodTable.size()];
      for(int i = 0; i < groupMethodTable.size(); i++) {
        if(groupMethodTable.get(i).getFrame().getSymbols().iterator().hasNext()) {
          length[i] = groupMethodTable.get(i).getFrame().getSymbols().iterator().next().getPrintName().split("#").length;
        } else {
          length[i] = 0;
        }
      }
      signatureLengths.put("group", length);
    }
    
    return signatureLengths;
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
  private static Map<String, List<SelectedMethod>> findMatchingMethods(Context context, Map<String, List<Environment>> mapMethodTableLists,
                                                          Map<String, List<List<MethodRanking>>> mapSignatureList) {
    
    Map<String, List<SelectedMethod>> mapListMethods = new HashMap<>();
    
    for(int e = 0; e < mapSignatureList.size(); e++) {
      List<SelectedMethod> selectedMethods = new ArrayList<>();
      String type = mapSignatureList.keySet().toArray(new String[0])[e];
      List<List<MethodRanking>> rankings = mapSignatureList.get(type);
      List<Environment> methodTableList = mapMethodTableLists.get(type);
      
      for(int i = 0; i < rankings.size(); i++) {
        List<MethodRanking> rankedMethodsList = rankings.get(i);
        String inputSignature = rankedMethodsList.get(0).getSignature();
    
        for (MethodRanking rankedMethod : rankedMethodsList) {
          String signature = rankedMethod.getSignature();
          double rank = rankedMethod.getRank();
          int dist = rankedMethod.getTotalDist();
          boolean has0 = rankedMethod.hasZeroDistanceArgument();
          Symbol signatureSymbol = Symbol.get(signature);
          SEXP function = methodTableList.get(i).getFrame().getVariable(signatureSymbol).force(context);
      
          if (function instanceof Closure) {
            selectedMethods.add(new SelectedMethod((Closure) function, type, rank, dist, signature, signatureSymbol, inputSignature, has0));
          }
        }
      }
      if(selectedMethods.size() > 0) {
        Collections.sort(selectedMethods);
        mapListMethods.put(type, selectedMethods);
      }
    }
    
    return mapListMethods;
  }
  
  /**
   * Provided a PairList of arguments and the number of arguments used for signature
   * this function first evaluates the arguments and extracts their class and superclass
   * information.
   *
   * */
  private static Map<String, List<List<MethodRanking>>> generateSignatures(Context context, Map<String, List<Environment>> mapMethodTableLists,
                                                                           PairList inputArgs, Map<String, int[]> depths) {
    
    Map<String, List<List<MethodRanking>>> mapListMethods = new HashMap<>();
    
    for(int e = 0; e < mapMethodTableLists.size(); e++) {
      String type = mapMethodTableLists.keySet().toArray(new String[0])[e];
      List<Environment> methodTableList = mapMethodTableLists.get(type);
      List<List<MethodRanking>> listSignatures = new ArrayList<>();
      int[] depth = depths.get(type);
      
      for(int listIdx = 0; listIdx < methodTableList.size(); listIdx++) {
        Environment methodTable = methodTableList.get(listIdx);
        int currentDepth = depth[listIdx];
        ArgumentSignature[] argSignatures;
        Symbol methodSymbol = methodTable.getFrame().getSymbols().iterator().next();
        Closure genericClosure = (Closure) methodTable.getFrame().getVariable(methodSymbol);
        PairList formals = genericClosure.getFormals();
        
        // when length of all arguments used in function call is as long as formals length (except ...)
        // then arguments are matched positionally and the argument tags are ignored
        // Example:
        // > setClass("ABC")
        // > obj <- new("ABC")
        // > obj[[i=1,j="hello"]] <- 100
        // `[[` formals are x(i, j, ..., value)
        // obj signature is:   x=ABC, i=numeric, j=character, value=numeric
        //
        // > obj[[j=1,i="hello"] <- 100
        // obj signature is:   x=ABC, i=numeric, j=character, value=numeric
        //
        // > obj[[j=1]] <- 100
        // obj signature is:   x=ABC, i=missing, j=numeric,   value=numeric
  
        PairList matchedList = ClosureDispatcher.matchArguments(formals, inputArgs, true);
        Map<Symbol, SEXP> matchedMap = new HashMap<>();
        for (PairList.Node node : matchedList.nodes()) {
          matchedMap.put(node.getTag(), node.getValue());
        }
        
        List<SEXP> inputMap = new ArrayList<>();
        for (PairList.Node node : inputArgs.nodes()) {
          inputMap.add(node.getValue());
        }
        int matchLength = matchedMap.containsKey(Symbols.ELLIPSES) ? matchedMap.size()-1 : matchedMap.size();
        boolean lengthMatchEqualsInput =  (matchLength - inputMap.size()) == 0;
  
        if (lengthMatchEqualsInput) {
          argSignatures = computeArgumentSignatures(context, inputArgs.nodes(), null, currentDepth);
        } else {
          argSignatures = computeArgumentSignatures(context, formals.nodes(), matchedMap, currentDepth);
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
        listSignatures.add(possibleSignatures);
      }
  
      mapListMethods.put(type, listSignatures);
    }
    
    return mapListMethods;
  }
  
  private static ArgumentSignature[] computeArgumentSignatures(Context context, Iterable<PairList.Node> nodes, Map<Symbol, SEXP> matchedMap, int currentDepth) {
  
    ArgumentSignature[] argSignatures = new ArgumentSignature[currentDepth];
  
    int idx = 0;
    for (PairList.Node node : nodes) {
      SEXP value;
      Symbol formalName;
      if (matchedMap == null) {
        value = node.getValue().force(context);
      } else {
        formalName = node.getTag();
        if(formalName != Symbols.ELLIPSES) {
          value = matchedMap.get(formalName).force(context);
        } else {
          continue;
        }
      }
      
      argSignatures[idx] = getArgumentSignature(context, value);
      idx++;
      
      if(idx >= argSignatures.length) {
        break;
      }
    }
    
    return argSignatures;
  }
  
  private static ArgumentSignature getArgumentSignature(Context context, SEXP argValue) {
    if (argValue == Symbol.MISSING_ARG) {
      return new ArgumentSignature();
    }
    String[] nodeClass = computeDataClasses(context, argValue).toArray();

    return getClassAndDistance(context, nodeClass);
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
    Environment environment = context.getEnvironment();
    AttributeMap map = environment.findVariable(context, argClassObjectName).force(context).getAttributes();
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
  
  public static SEXP computeDataClassesS4(Context context, String className) {
    Symbol argClassObjectName = Symbol.get(".__C__" + className);
    Environment environment = context.getEnvironment();
    AttributeMap map = environment.findVariable(context, argClassObjectName).force(context).getAttributes();
    return map.get("contains").getNames();
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
      } else {
        newArgs.add(node.getRawTag(), evaluatedArguments.get(varArgIndex++));
      }
    }

    // When dispatching to S3 summary methods, we pretend that the summary
    // builtin has an extra na.rm argument with default value false.

    if(!naRmArgumentSupplied) {
      newArgs.add(NA_RM, LogicalArrayVector.FALSE);
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
  
      StringVector objectClasses = computeDataClasses(context, object);
      if(Types.isS4(object)) {
        SEXP objectClassesS4 = computeDataClassesS4(context, objectClasses.getElementAsString(0));
        if(objectClassesS4 != Null.INSTANCE) {
          resolver.classes = Lists.newArrayList((StringVector)objectClassesS4);
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
        if(Types.isS4(object) && isS4DispatchSupported(genericMethodName)) {
          List<Environment> methodTables = findMethodTable(context, genericMethodName);
          if(methodTables != null) {
            Iterator<Environment> methodTableItr = methodTables.iterator();
            while (method == null && methodTableItr.hasNext()) {
              Environment env = methodTableItr.next();
              SEXP methodName = env.getFrame().getVariable(Symbol.get(className));
              if(methodName instanceof GenericMethod) {
                method = (GenericMethod) methodName;
              }
            }
            if(method != null) {
              return method;
            }
          }
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
    
    public ArgumentSignature(String[] classes, int[] distances) {
      this.argumentClasses = classes;
      this.distances = distances;
    }
    
    public ArgumentSignature() {
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
  
  public static class MethodRanking {
    private String signature;
    private int[] distances;
    private boolean has0 = false;
    private int totalDist = 0;
    private double rank = 0.0;
    
    public MethodRanking(String signature, int[] distance) {
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
    
    public double getRank() {
      return rank;
    }
    
    public boolean hasZeroDistanceArgument() {
      return has0;
    }
  }
  
  public static class SelectedMethod implements Comparable <SelectedMethod> {
    private Closure function;
    private String group;
    private double currentRank;
    private boolean has0;
    private int currentDist;
    private String currentSig;
    private Symbol methodName;
    private String methodInputSignature;
    
    public SelectedMethod(Closure fun, String grp, double rank, int dist, String sig, Symbol method, String methSig, boolean has0) {
      this.function = fun;
      this.group = grp;
      this.currentRank = rank;
      this.currentDist = dist;
      this.currentSig = sig;
      this.methodName = method;
      this.methodInputSignature = methSig;
      this.has0 = has0;
    }
    
    public Closure getFunction() {
      return function;
    }
    
    public String getGroup() {
      return group;
    }
  
    public double getRank() {
      return currentRank;
    }
  
    public double getDistance() {
      return currentDist;
    }
    
    public String getSignature() {
      return currentSig;
    }
    
    public Symbol getMethod() {
      return methodName;
    }
    
    public int getTotalDist() {
      return currentDist;
    }
    
    public int isHas0() {
      if(has0) {
        return 0;
      } else {
        return 1;
      }
    }
  
    @Override
    public int compareTo(SelectedMethod o) {
      int i = Integer.compare(this.getTotalDist(), o.getTotalDist());
      if (i != 0) {
        return i;
      }
      i = Integer.compare(this.isHas0(), o.isHas0());
      if (i != 0) {
        return i;
      }
      return Double.compare(currentRank, o.getRank());
    }
  }
}
