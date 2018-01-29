/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.renjin.eval.Calls;
import org.renjin.eval.ClosureDispatcher;
import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.repackaged.guava.primitives.Ints;
import org.renjin.sexp.*;
import org.renjin.sexp.Vector;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class S4 {


  private static final Set<String> ARITH_GROUP = Sets.newHashSet("+", "-", "*", "^", "%%", "%/%", "/");

  private static final Set<String> COMPARE_GROUP = Sets.newHashSet("==", ">", "<", "!=", "<=", ">=");

  private static final Set<String> LOGIC_GROUP = Sets.newHashSet("&", "&&", "|", "||", "xor");

  public static SEXP handleS4object(@Current Context context, SEXP source, PairList args,
                                    Environment rho, String group, String opName) {

    if ("as.double".equals(opName)) {
      opName = "as.numeric";
    }


    List<Environment> groupMethodTables = null;
    List<Environment> genericMethodTables = null;

    genericMethodTables = findMethodTable(context, opName);
    if ("Ops".equals(group)) {
      groupMethodTables = findOpsMethodTable(context, opName);
    } else if (!("".equals(group) || group == null)) {
      groupMethodTables = findMethodTable(context, group);
    }

    if ((groupMethodTables == null || groupMethodTables.size() == 0) && (genericMethodTables == null || genericMethodTables.size() == 0)) {
      return null;
    }

    Map<String, List<Environment>> mapMethodTableList = new HashMap<>();
    if (genericMethodTables != null && genericMethodTables.size() != 0) {
      mapMethodTableList.put("generic", genericMethodTables);
    }
    if (groupMethodTables != null && groupMethodTables.size() != 0) {
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

    if (maxSignatureLength == 0) {
      return null;
    }

    // expand ... in arguments or remove if empty
    PairList expandedArgs = Calls.promiseArgs(args, context, rho);
    PairList.Builder promisedArgs = new PairList.Builder();
    Iterator<PairList.Node> it = expandedArgs.nodes().iterator();
    int argIdx = 0;
    while (it.hasNext()) {
      PairList.Node node = it.next();
      SEXP uneval = node.getValue();
      if (argIdx == 0) {
        // The source has already been evaluated to check for class
        promisedArgs.add(node.getRawTag(), new Promise(uneval, source));
      } else {
        // otherwise a promise is created to evaluate if necessary
        if (uneval == Symbol.MISSING_ARG) {
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

    if (validMethods.keySet().size() == 0) {
      return null;
    }

    int maxNumberOfMethods = 0;
    Iterator<String> typeItr = validMethods.keySet().iterator();
    for (; typeItr.hasNext(); ) {
      List<SelectedMethod> methods = validMethods.get(typeItr.next());
      int nextSize = methods.size();
      if (nextSize > maxNumberOfMethods) {
        maxNumberOfMethods = nextSize;
      }
    }

    if (maxNumberOfMethods == 0) {
      return null;
    }

    SelectedMethod method;
    SelectedMethod genericMethod = null;
    SelectedMethod groupMethod = null;
    double genericRank = -1;
    double groupRank = -1;
    if (validMethods.containsKey("generic")) {
      genericRank = validMethods.get("generic").get(0).getRank();
      genericMethod = validMethods.get("generic").get(0);
    }
    if (validMethods.containsKey("group")) {
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
    boolean hasS3Class = source.getAttribute(Symbol.get(".S3Class")).length() != 0;
    boolean genericExact = "generic".equals(method.getGroup()) && method.getTotalDist() == 0;

    if (!opName.contains("<-") && (genericExact || hasS3Class)) {
      SEXP call = new FunctionCall(function, promisedArgs.build());
      return context.evaluate(call);
    } else {
      Map<Symbol, SEXP> metadata = new HashMap<>();
      metadata.put(Symbol.get(".defined"), buildDotTargetOrDefined(context, method, true));
      metadata.put(Symbol.get(".Generic"), buildDotGeneric(opName));
      metadata.put(Symbol.get(".Method"), function);
      metadata.put(Symbol.get(".Methods"), Symbol.get(".Primitive(\"" + opName + "\")"));
      metadata.put(Symbol.get(".target"), buildDotTargetOrDefined(context, method, false));

      PairList formals = function.getFormals();
      PairList matchedList = ClosureDispatcher.matchArguments(formals, promisedArgs.build(), true);
      Map<Symbol, SEXP> matchedMap = new HashMap<>();
      for (PairList.Node node : matchedList.nodes()) {
        matchedMap.put(node.getTag(), node.getValue());
      }

      for (Symbol arg : matchedMap.keySet()) {
        SEXP argValue = matchedMap.get(arg);
        if (argValue != Symbol.MISSING_ARG) {
          if (argValue instanceof Promise && ((Promise) argValue).getValue() != null) {
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
    for (String type : types) {
      if (signatureLengths.containsKey(type)) {
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
   * <p>
   * For '.defined', the class of selected method formals are used. 'methods' is
   * used for the selected method arguments of class "ANY" or atomic vectors.
   * <p>
   * > setClass("A", representation(a="numeric"))
   * > setMethod("[", signature("A","A","ANY"), function(x,i,j,...) environment())
   * > x <- a[a,1]
   * > cat(deparse( attr(x$.target, "package") ))
   * c("methods", "methods","methods")
   * > cat(deparse( attr(x$.defined, "package") ))
   * c("methods", ".GlobalEnv","methods")
   */
  private static SEXP buildDotTargetOrDefined(Context context, SelectedMethod method, boolean defined) {

    List<String> argumentClasses;
    argumentClasses = Arrays.asList(method.getSignature().split("#"));

    List<String> argumentPackages = new ArrayList<>();

    if (defined) {
      for (String argumentClass : argumentClasses) {
        argumentPackages.add(findClassOrMethodName(context, argumentClass, ".__C__", "methods", false));
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

  public static List<Environment> findMethodTable(Context context, String opName) {
    String genericName = findClassOrMethodName(context, opName, ".__T__", "base", true);
    List<Environment> methodTableList = new CopyOnWriteArrayList<>();

    List<String> pkgNames = getNamesLoadedPackages(context);
    pkgNames.add(0, ".GlobalEnv");
    for (String pkg : pkgNames) {
      SEXP methodTablePackage = getFromPackage(context, pkg, genericName);
      if (methodTablePackage instanceof Environment) {
        methodTableList.add((Environment) methodTablePackage);
      }
    }

    return methodTableList.size() == 0 ? null : methodTableList;
  }

  private static String findClassOrMethodName(Context context, String name, String what, String altValue, boolean method) {

    String sourcePackage = null;
    String className = what + name;

    List<String> loadedPackages = getNamesLoadedPackages(context);
    loadedPackages.add(0, ".GlobalEnv");

    for (int i = 0; i < loadedPackages.size() && sourcePackage == null; i++) {
      String pkgName = loadedPackages.get(i);
      String methodName = what + name + ":" + pkgName;
      String generic = method ? methodName : className;
      SEXP methodTable = getFromPackage(context, pkgName, generic);
      if (methodTable instanceof Environment || methodTable instanceof S4Object) {
        sourcePackage = method ? methodName : pkgName;
      }
    }

    if (sourcePackage == null) {
      sourcePackage = method ? what + name + ":" + altValue : altValue;
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

    for (String pkg : getNamesLoadedPackages(context)) {
      Frame packageFrame = getPackageFrame(context, pkg);
      SEXP methodTablePackage = getMethodTable(context, opName, packageFrame);
      if (methodTablePackage instanceof Environment &&
          ((Environment) methodTablePackage).getFrame().getSymbols().size() > 0) {
        methodTableList.add((Environment) methodTablePackage);
      }
    }

    if (methodTableList.size() == 0) {
      return null;
    }

    return methodTableList;
  }

  public static Frame getPackageFrame(Context context, String name) {
    if (".GlobalEnv".equals(name)) {
      return context.getGlobalEnvironment().getFrame();
    }
    Namespace pkgNamespace = context.getNamespaceRegistry().getNamespace(context, name);
    return pkgNamespace.getNamespaceEnvironment().getFrame();
  }

  public static SEXP getFromPackage(Context context, String pkg, String what) {
    Frame pkgFrame = getPackageFrame(context, pkg);
    return pkgFrame.getVariable(Symbol.get(what)).force(context);
  }

  public static List<String> getNamesLoadedPackages(Context context) {
    List<String> loadedPackages = new CopyOnWriteArrayList<>();
    for (Symbol symbol : context.getNamespaceRegistry().getLoadedNamespaces()) {
      loadedPackages.add(symbol.getPrintName());
    }
    return loadedPackages;
  }

  private static SEXP getMethodTable(Context context, String opName, Frame packageFrame) {
    SEXP methodTable = null;
    if (ARITH_GROUP.contains(opName)) {
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

    if (genericMethodTable != null) {
      int[] length = new int[genericMethodTable.size()];
      for (int i = 0; i < genericMethodTable.size(); i++) {
        if (genericMethodTable.get(i).getFrame().getSymbols().iterator().hasNext()) {
          length[i] = genericMethodTable.get(i).getFrame().getSymbols().iterator().next().getPrintName().split("#").length;
        } else {
          length[i] = 0;
        }
      }
      signatureLengths.put("generic", length);
    }

    if (groupMethodTable != null) {
      int[] length = new int[groupMethodTable.size()];
      for (int i = 0; i < groupMethodTable.size(); i++) {
        if (groupMethodTable.get(i).getFrame().getSymbols().iterator().hasNext()) {
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
   * This function loops through an ArrayList of generated MethodRankings and stores the signatures for
   * which a method can be found, together with the method and the accompanying meta data from MethodRanking
   * class in an new ArrayList<SelectedMethod>. The ArrayList<MethodRanking> input should be sorted before
   * calling this function.
   * <p>
   * The selected method, method signature, total distance, and method type (generic or group) information
   * are stored in SelectedMethod class. This is necessary to be able to give generic methods priority over
   * group methods, and also for giving a warning for ignored valid methods.
   */
  private static Map<String, List<SelectedMethod>> findMatchingMethods(Context context, Map<String, List<Environment>> mapMethodTableLists,
                                                                       Map<String, List<List<MethodRanking>>> mapSignatureList) {

    Map<String, List<SelectedMethod>> mapListMethods = new HashMap<>();

    for (int e = 0; e < mapSignatureList.size(); e++) {
      List<SelectedMethod> selectedMethods = new ArrayList<>();
      String type = mapSignatureList.keySet().toArray(new String[0])[e];
      List<List<MethodRanking>> rankings = mapSignatureList.get(type);
      List<Environment> methodTableList = mapMethodTableLists.get(type);

      for (int i = 0; i < rankings.size(); i++) {
        List<MethodRanking> rankedMethodsList = rankings.get(i);
        String inputSignature = rankedMethodsList.get(0).getSignature();

        for (MethodRanking rankedMethod : rankedMethodsList) {
          String signature = rankedMethod.getSignature();
          double rank = rankedMethod.getRank();
          int[] dist = rankedMethod.getDistances();
          boolean has0 = rankedMethod.hasZeroDistanceArgument();
          Symbol signatureSymbol = Symbol.get(signature);
          SEXP function = methodTableList.get(i).getFrame().getVariable(signatureSymbol).force(context);

          if (function instanceof Closure) {
            selectedMethods.add(new SelectedMethod((Closure) function, type, rank, dist, signature, signatureSymbol, inputSignature, has0));
          }
        }
      }
      if (selectedMethods.size() > 0) {
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
   */
  private static Map<String, List<List<MethodRanking>>> generateSignatures(Context context, Map<String, List<Environment>> mapMethodTableLists,
                                                                           PairList inputArgs, Map<String, int[]> depths) {

    Map<String, List<List<MethodRanking>>> mapListMethods = new HashMap<>();

    for (int e = 0; e < mapMethodTableLists.size(); e++) {
      String type = mapMethodTableLists.keySet().toArray(new String[0])[e];
      List<Environment> methodTableList = mapMethodTableLists.get(type);
      List<List<MethodRanking>> listSignatures = new ArrayList<>();
      int[] depth = depths.get(type);

      for (int listIdx = 0; listIdx < methodTableList.size(); listIdx++) {
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
        int matchLength = matchedMap.containsKey(Symbols.ELLIPSES) ? matchedMap.size() - 1 : matchedMap.size();
        boolean lengthMatchEqualsInput = (matchLength - inputMap.size()) == 0;

        if (lengthMatchEqualsInput) {
          argSignatures = computeArgumentSignatures(context, inputArgs.nodes(), null, currentDepth);
        } else {
          argSignatures = computeArgumentSignatures(context, formals.nodes(), matchedMap, currentDepth);
        }

        int numberOfPossibleSignatures = 1;
        for (int i = 0; i < argSignatures.length; i++) {
          numberOfPossibleSignatures = numberOfPossibleSignatures * argSignatures[i].getArgument().length;
        }

        List<MethodRanking> possibleSignatures = new ArrayList<>(numberOfPossibleSignatures);

        int numberOfClassesCurrentArgument;
        int argumentClassIdx = 0;
        int repeat = 1;
        int repeatIdx = 1;

        for (int col = 0; col < depth[listIdx]; col++) {
          numberOfClassesCurrentArgument = argSignatures[col].getArgument().length;
          for (int row = 0; row < numberOfPossibleSignatures; row++, repeatIdx++) {
            if (argumentClassIdx == numberOfClassesCurrentArgument) {
              argumentClassIdx = 0;
            }
            ArgumentSignature argSignature = argSignatures[col];
            String signature = argSignature.getArgument(argumentClassIdx);
            if (possibleSignatures.isEmpty() ||
                possibleSignatures.size() < row + 1 ||
                possibleSignatures.get(row) == null) {
              int[] distance = argSignature.getDistanceAsArray(argumentClassIdx);
              possibleSignatures.add(row, new MethodRanking(signature, distance));
            } else {
              int distance = argSignature.getDistance(argumentClassIdx);
              possibleSignatures.set(row, possibleSignatures.get(row).append(signature, distance));
            }
            if (repeat == 1) {
              argumentClassIdx++;
            }
            if (repeat != 1 && repeatIdx == repeat) {
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
        if (formalName != Symbols.ELLIPSES) {
          value = matchedMap.get(formalName).force(context);
        } else {
          continue;
        }
      }

      argSignatures[idx] = getArgumentSignature(context, value);
      idx++;

      if (idx >= argSignatures.length) {
        break;
      }
    }

    return argSignatures;
  }

  private static ArgumentSignature getArgumentSignature(Context context, SEXP argValue) {
    if (argValue == Symbol.MISSING_ARG) {
      return new ArgumentSignature();
    }
    String[] nodeClass = S3.computeDataClasses(context, argValue).toArray();
    List<String> listClasses = new ArrayList<>(Arrays.asList(nodeClass));
    if (listClasses.contains("double")) {
      listClasses.remove("double");
    }

    return getClassAndDistance(context, listClasses);
  }

  /**
   * Each class is stored as an S4 object with prefix '.__C__'. This S4 object
   * has a attribute 'contains' where the superclass names and distance are stored.
   * The first class is the input class with distance of 0. All of the super classes
   * and their distance are added using the names of 'contains' and the accompanying
   * value in 'distance' slot. "ANY" is added as the last class and the distance is
   * defined as the maximum distance + 1. The class names and distances are stored in
   * an ArgumentSignature class and return.
   */

  private static ArgumentSignature getClassAndDistance(Context context, List<String> argClass) {

    List<Integer> distances = new ArrayList<>();
    List<String> classes = new ArrayList<>();
    for (int i = 0; i < argClass.size(); i++) {
      classes.add(argClass.get(i));
      distances.add(0);
    }

    SEXP containsSlot = getContainsSlot(context, argClass.get(0));
    SEXP argSuperClasses = getSuperClassesS4(containsSlot);

    for (int i = 0; i < argSuperClasses.length(); i++) {
      SEXP distanceSlot = ((ListVector) containsSlot).get(i).getAttributes().get("distance");
      distances.add(((Vector) distanceSlot).getElementAsInt(0));
      classes.add(((Vector) argSuperClasses).getElementAsString(i));
    }

    int max = Collections.max(distances);
    if (!classes.contains("ANY") && !classes.contains("NULL")) {
      distances.add(max + 1);
      classes.add("ANY");
    }

    return new ArgumentSignature(classes.toArray(new String[0]), Ints.toArray(distances));
  }

  public static SEXP getContainsSlot(Context context, String objClass) {
    Namespace ns = context.getNamespaceRegistry().getNamespace(context, "methods");
    SEXP env = ns.getNamespaceEnvironment();
    SEXP classTable = ((Environment) env).findVariableUnsafe(Symbol.get(".classTable")).force(context);
    AttributeMap map = ((Environment) classTable).findVariable(context, Symbol.get(objClass)).getAttributes();
    return map.get("contains");
  }

  public static AtomicVector getSuperClassesS4(Context context, String objClass) {
    SEXP containsSlot = getContainsSlot(context, objClass);
    return containsSlot.getNames();
  }

  public static SEXP getSuperClassesS4(SEXP containsSlot) {
    return containsSlot.getNames();
  }

  public static SEXP computeDataClassesS4(Context context, String className) {
    Symbol argClassObjectName = Symbol.get(".__C__" + className);
    Environment environment = context.getEnvironment();
    AttributeMap map = environment.findVariable(context, argClassObjectName).force(context).getAttributes();
    return map.get("contains").getNames();
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
      for (int i = 0; i < distance.length; i++) {
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
      for (int i = 0; i < this.distances.length; i++) {
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

    public int[] getDistances() {
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

  public static class SelectedMethod implements Comparable<SelectedMethod> {
    private Closure function;
    private String group;
    private double currentRank;
    private boolean has0;
    private int[] distances;
    private int currentDist;
    private String currentSig;
    private Symbol methodName;
    private String methodInputSignature;

    public SelectedMethod(Closure fun, String grp, double rank, int[] dist, String sig, Symbol method, String methSig, boolean has0) {
      this.function = fun;
      this.group = grp;
      this.currentRank = rank;
      this.distances = dist;
      int sum = 0;
      for (int i = 0; i < dist.length; i++) {
        sum += dist[i];
      }
      this.currentDist = sum;
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

    public int[] getDistances() {
      return distances;
    }

    public int getDistance(int i) {
      int sum = 0;
      for (int j = 0; j < i; j++) {
        sum += distances[j];
      }
      return sum;
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
      if (has0) {
        return 0;
      } else {
        return 1;
      }
    }

    @Override
    public int compareTo(SelectedMethod o) {
      int i = Integer.compare(this.isHas0(), o.isHas0());
      if (i != 0) {
        return i;
      }

      if (this.getDistances().length == o.getDistances().length) {
        i = Integer.compare(this.getTotalDist(), o.getTotalDist());
        if (i != 0) {
          return i;
        }
      } else {
        int minArgs = Math.min(this.getDistances().length, o.getDistances().length);
        i = Integer.compare(this.getDistance(minArgs), o.getDistance(minArgs));
        if (i != 0) {
          return i;
        }
      }

      return Double.compare(currentRank, o.getRank());
    }
  }
}
