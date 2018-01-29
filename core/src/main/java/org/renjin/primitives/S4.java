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

public class S4 {


  private static final Set<String> ARITH_GROUP = Sets.newHashSet("+", "-", "*", "^", "%%", "%/%", "/");

  private static final Set<String> COMPARE_GROUP = Sets.newHashSet("==", ">", "<", "!=", "<=", ">=");

  private static final Set<String> LOGIC_GROUP = Sets.newHashSet("&", "&&", "|", "||", "xor");


  public static SEXP handleS4object(@Current Context context, SEXP source, PairList args,
                                    Environment rho, String group, String opName) {

    if("as.double".equals(opName)) {
      opName = "as.numeric";
    }

    S4Cache cache = new S4Cache();

    cache.addGeneric(getLoadedPackagesMethods(context, opName));
    if("Ops".equals(group)) {
      cache.addGroup(getLoadedPackagesMethodsOps(context, opName));
    } else if(!("".equals(group) || group == null)) {
      cache.addGroup(getLoadedPackagesMethods(context, group));
    }

    if(cache.hasNoMethods()) {
      return null;
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

    S4Cache.findMaxSignatureLength();

    if(S4Cache.getMaxSignatureLength() == 0) {
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

    Signatures possibleSignatures = generateSignatures(context, cache, promisedArgs.build());

    // The generated signatures are sorted based on their distance to input signature and looked up in originating
    // method table. All signatures are looked up and the ones present are returned.

    SelectedMethod method = findMatchingMethods(context, cache, possibleSignatures);

    if (method == null) {
      return null;
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

  private static MethodTable getLoadedPackagesMethodsOps(Context context, String opName) {
    MethodTable methodTable = null;
    if(ARITH_GROUP.contains(opName)) {
      String[] groups = {"Arith", "Ops"};
      for (int i = 0; i < groups.length && methodTable == null; i++) {
        MethodTable foundMethodTable = getLoadedPackagesMethods(context, groups[i]);
        methodTable = foundMethodTable != null ? foundMethodTable : methodTable;
      }
    } else if (COMPARE_GROUP.contains(opName)) {
      String[] groups = {"Compare", "Ops"};
      for (int i = 0; i < groups.length && methodTable == null; i++) {
        MethodTable foundMethodTable = getLoadedPackagesMethods(context, groups[i]);
        methodTable = foundMethodTable != null ? foundMethodTable : methodTable;
      }
    } else if (LOGIC_GROUP.contains(opName)) {
      String[] groups = {"Logic", "Ops"};
      for (int i = 0; i < groups.length && methodTable == null; i++) {
        MethodTable foundMethodTable = getLoadedPackagesMethods(context, groups[i]);
        methodTable = foundMethodTable != null ? foundMethodTable : methodTable;
      }
    }
    return methodTable;
  }

  public static class MethodTable {
    List<String> packageNames;
    List<Environment> methods;
    List<Integer> sigLengths;

    public MethodTable(List<String> packages, List<Environment> methods, List<Integer> lengths) {
      this.packageNames = packages;
      this.methods = methods;
      this.sigLengths = lengths;
    }
  }

  public static MethodTable getLoadedPackagesMethods(Context context, String opName) {
    String what = ".__T__" + opName + ":";
    List<String> packageNames = new ArrayList<>();
    List<Environment> packagesMethods = new ArrayList<>();
    List<Integer> sigLengths = new ArrayList<>();

    for(Symbol namespace : context.getNamespaceRegistry().getLoadedNamespaces()) {
      String packageName = namespace.getPrintName();
      Environment method = null;
      int sigLen = 0;
      Frame namespaceFrame = context.getNamespaceRegistry().getNamespace(context, namespace).getNamespaceEnvironment().getFrame();
      Iterator<Symbol> symItr = namespaceFrame.getSymbols().iterator();

      while(symItr.hasNext() && method == null) {
        Symbol symbol = symItr.next();
        if(symbol.getPrintName().startsWith(what)) {
          method = (Environment) namespaceFrame.getVariable(symbol);
          sigLen = method.getFrame().getSymbols().iterator().next().getPrintName().split("#").length;
        }
      }

      if(method != null) {
        packageNames.add(packageName);
        packagesMethods.add(method);
        sigLengths.add(sigLen);
      }
    }

    if(packagesMethods.size() == 0) {
      return null;
    } else {
      return new MethodTable(packageNames, packagesMethods, sigLengths);
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
    List<Environment> methodTableList = new ArrayList<>();

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
    List<String> loadedPackages = new ArrayList<>();
    for (Symbol symbol : context.getNamespaceRegistry().getLoadedNamespaces()) {
      loadedPackages.add(symbol.getPrintName());
    }
    return loadedPackages;
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
  private static SelectedMethod findMatchingMethods(Context context, S4Cache cache, Signatures signatures) {
    List<SelectedMethod> genericMethods = new ArrayList<>();
    List<SelectedMethod> groupMethods = new ArrayList<>();
    SelectedMethod topGeneric = null;
    SelectedMethod topGroup = null;

    List<MethodRanking> rankings = signatures.genericRankings;
    String inputSignature = rankings.get(0).getSignature();

    for(int i = 0; i < rankings.size(); i++) {
      MethodRanking rankedMethod = rankings.get(i);
      String signature = rankedMethod.getSignature();
      double rank = rankedMethod.getRank();
      int[] dist = rankedMethod.getDistances();
      boolean has0 = rankedMethod.hasZeroDistanceArgument();
      Symbol signatureSymbol = Symbol.get(signature);

      if(cache.hasGeneric()) {
        List<Environment> genericMethodTables = cache.getGenericMethodTables();
        SEXP genericFunction = genericMethodTables.get(i).getFrame().getVariable(signatureSymbol).force(context);
        if(genericFunction instanceof Closure) {
          SelectedMethod genericMethod = new SelectedMethod((Closure) genericFunction, "generic", rank, dist, signature, signatureSymbol, inputSignature, has0);
          genericMethods.add(genericMethod);
        }
      }

      if(cache.hasGroup()) {
        List<Environment> groupMethodTables = cache.getGroupMethodTables();
        SEXP groupFunction = groupMethodTables.get(i).getFrame().getVariable(signatureSymbol).force(context);
        if(groupFunction instanceof Closure) {
          SelectedMethod groupMethod = new SelectedMethod((Closure) groupFunction, "group", rank, dist, signature, signatureSymbol, inputSignature, has0);
          groupMethods.add(groupMethod);
        }
      }
    }

    if (genericMethods.size() > 0) {
      Collections.sort(genericMethods);
      topGeneric = genericMethods.get(0);
    }

    if(groupMethods.size() > 0) {
      Collections.sort(groupMethods);
      topGroup = groupMethods.get(0);
    }

    if(topGeneric == null && topGroup == null) {
      return null;
    } else if (topGeneric == null) {
      return topGroup;
    } else if (topGroup == null) {
      return topGeneric;
    } else if(genericMethods.get(0).getRank() > groupMethods.get(0).getRank()) {
      return topGroup;
    } else {
      return topGeneric;
    }
  }

  /**
   * Provided a PairList of arguments and the number of arguments used for signature
   * this function first evaluates the arguments and extracts their class and superclass
   * information.
   */
  private static Signatures generateSignatures(Context context, S4Cache cache, PairList inputArgs) {

    List<MethodRanking> genericSignatures = new ArrayList<>();
    List<MethodRanking> groupSignatures = new ArrayList<>();

    if(cache.hasGeneric()) {
      List<Integer> signatureLengths = S4Cache.getGenericSignatureLengths();
      List<Environment> methodTables = S4Cache.getGenericMethodTables();

      for (int listIdx = 0; listIdx < methodTables.size(); listIdx++) {
        List<MethodRanking> possibleSignatures = createSignatureFromMethodTable(context, inputArgs, methodTables, listIdx, signatureLengths.get(listIdx));
        genericSignatures.addAll(possibleSignatures);
      }
    }

    if(cache.hasGroup()) {
      List<Integer> signatureLengths = S4Cache.getGroupSignatureLengths();
      List<Environment> methodTables = S4Cache.getGroupMethodTables();

      for (int listIdx = 0; listIdx < methodTables.size(); listIdx++) {
        List<MethodRanking> possibleSignatures = createSignatureFromMethodTable(context, inputArgs, methodTables, listIdx, signatureLengths.get(listIdx));
        groupSignatures.addAll(possibleSignatures);
      }
    }

    return new Signatures(genericSignatures, groupSignatures);
  }

  private static List<MethodRanking> createSignatureFromMethodTable(Context context, PairList inputArgs, List<Environment> methodTables, int listIdx, Integer integer) {
    Environment methodTable = methodTables.get(listIdx);
    int currentDepth = integer;
    ArgumentSignature[] argSignatures;
    Symbol methodSymbol = methodTable.getFrame().getSymbols().iterator().next();
    Closure genericClosure = (Closure) methodTable.getFrame().getVariable(methodSymbol);
    PairList formals = genericClosure.getFormals();

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

    return createSignatures(argSignatures, integer);
  }

  private static List<MethodRanking> createSignatures(ArgumentSignature[] argSignatures, Integer integer) {

    int numberOfPossibleSignatures = 1;
    for (int i = 0; i < argSignatures.length; i++) {
      numberOfPossibleSignatures = numberOfPossibleSignatures * argSignatures[i].getArgument().length;
    }

    List<MethodRanking> possibleSignatures = new ArrayList<>(numberOfPossibleSignatures);

    int numberOfClassesCurrentArgument;
    int argumentClassIdx = 0;
    int repeat = 1;
    int repeatIdx = 1;

    for (int col = 0; col < integer; col++) {
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
    return possibleSignatures;
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

  public static class S4Cache {
    private static List<Environment> genericMethodTables = new ArrayList<>();
    private static List<Integer> genericSignatureLengths = new ArrayList<>();
    private static int maxGenericSignatureLength = 0;

    private static List<Environment> groupMethodTables = new ArrayList<>();
    private static List<Integer> groupSignatureLengths = new ArrayList<>();
    private static int maxGroupSignatureLength = 0;

    private static int maxSignatureLength = 0;

    private static List<Integer> getGenericSignatureLengths() {
      return genericSignatureLengths;
    }

    private static List<Integer> getGroupSignatureLengths() {
      return groupSignatureLengths;
    }

    private static List<Environment> getGenericMethodTables() {
      return genericMethodTables;
    }

    private static List<Environment> getGroupMethodTables() {
      return groupMethodTables;
    }

    private void addGeneric(MethodTable generic) {
      genericMethodTables = generic.methods;
      genericSignatureLengths = generic.sigLengths;
    }
    private void addGroup(MethodTable group) {
      groupMethodTables = group.methods;
      groupSignatureLengths = group.sigLengths;
    }

    private boolean hasGeneric() {
      return genericMethodTables.size() > 0;
    }
    private boolean hasGroup() {
      return groupMethodTables.size() > 0;
    }
    private boolean hasNoMethods() {
      return !hasGroup() && !hasGeneric();
    }
    private static void setMaxLength(int value) {
      maxSignatureLength = value;
    }
    private static void setMaxGenericLength(int value) {
      maxGenericSignatureLength = value;
    }
    private static void setMaxGroupLength(int value) {
      maxGroupSignatureLength = value;
    }
    private static int getMaxSignatureLength() {
      return maxSignatureLength;
    }

    private static void findMaxSignatureLength() {
      int maxGenericMethodLength = 0;
      int maxGroupMethodLength = 0;

      if(hasGeneric()) {
        int length = Collections.max(genericSignatureLengths);
        maxGenericMethodLength = length > maxGenericMethodLength ? length : maxGenericMethodLength;
        setMaxGenericLength(maxGenericMethodLength);
      }

      if(hasGroup()) {
        int length = Collections.max(groupSignatureLengths);
        maxGroupMethodLength = length > maxGroupMethodLength ? length : maxGroupMethodLength;
        setMaxGroupLength(maxGroupMethodLength);
      }

      setMaxLength(maxGenericMethodLength > maxGroupMethodLength ? maxGenericMethodLength : maxGroupMethodLength);
    }

  }

  public static class Signatures {
    private List<MethodRanking> genericRankings;
    private List<MethodRanking> groupRankings;
    public Signatures(List<MethodRanking> genericRankings, List<MethodRanking> groupRankings) {
      this.genericRankings = genericRankings;
      this.groupRankings = groupRankings;
    }
  }
}
