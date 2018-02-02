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
package org.renjin.s4;

import org.renjin.eval.ClosureDispatcher;
import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S4 {


  /**
   * Attempts to dispatch to an S4 method based on the calling arguments.
   *
   * <p>If a suitable method is found, it is evaluated and this method returns the result of the function call</p>
   *
   * <p>If no method is found, then this method returns {@code null}</p>
   */
  public static SEXP tryDispatchToS4Method(@Current Context context, SEXP source, PairList args,
                                           Environment rho, String group, String opName) {

    Generic generic = new Generic(opName, group);
    MethodLookupTable lookupTable = new MethodLookupTable(generic, context);
    if(lookupTable.isEmpty()) {
      return null;
    }

    CallingArguments arguments = new CallingArguments(context, rho, source, args, lookupTable.getArgumentMatcher());
    S4ClassCache classCache = new S4ClassCache(context);
    DistanceCalculator calculator = new DistanceCalculator(classCache);

    RankedMethod selectedMethod = lookupTable.selectMethod(arguments, calculator);
    if(selectedMethod == null) {
      return null;
    }

    Closure function = selectedMethod.getMethod().getDefinition();

    if (dispatchWithoutMeta(opName, source, selectedMethod)) {
      FunctionCall call = new FunctionCall(function, arguments.getPromisedArgs());
      return context.evaluate(call);
      
    } else {
      Map<Symbol, SEXP> metadata = generateCallMetaData(context, selectedMethod, opName, arguments.getPromisedArgs());
      FunctionCall call = new FunctionCall(function, arguments.getExpandedArgs());
      return ClosureDispatcher.apply(context, rho, call, function, arguments.getPromisedArgs(), metadata);
    }
  }

  private static boolean dispatchWithoutMeta(String opName, SEXP source, RankedMethod rank) {
    boolean hasS3Class = source.getAttribute(Symbol.get(".S3Class")).length() != 0;
    boolean genericExact = rank.getMethod().isGroupGeneric() && rank.isExact();
    return (!opName.contains("<-") && (genericExact || hasS3Class));
  }

  public static Map<Symbol, SEXP> generateCallMetaData(Context context, RankedMethod method, String opName, PairList promisedArgs) {
    Map<Symbol, SEXP> metadata = new HashMap<>();
    metadata.put(Symbol.get(".defined"), buildDotTargetOrDefined(context, method, true));
    metadata.put(Symbol.get(".Generic"), buildDotGeneric(opName));
    metadata.put(Symbol.get(".MethodLookupTable"), method.getFunction());
    metadata.put(Symbol.get(".Methods"), Symbol.get(".Primitive(\"" + opName + "\")"));
    metadata.put(Symbol.get(".target"), buildDotTargetOrDefined(context, method, false));

    PairList formals = method.getMethod().getFormals();
    
    // TODO: haven't we already matched arguments?
    PairList matchedList = ClosureDispatcher.matchArguments(formals, promisedArgs, true);
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
    return metadata;
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
  private static SEXP buildDotTargetOrDefined(Context context, RankedMethod method, boolean defined) {

    List<String> argumentClasses = method.getMethod().getSignature().getClasses();
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
        .setAttribute("names", method.getMethod().getFormalNames())
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
    for (Symbol symbol : context.getNamespaceRegistry().getLoadedNamespaceNames()) {
      loadedPackages.add(symbol.getPrintName());
    }
    return loadedPackages;
  }


  private static SEXP getContainsSlot(Context context, String objClass) {
    SEXP classDef = getClassDef(context, objClass);
    AttributeMap map = classDef.getAttributes();
    return map.get("contains");
  }

  private static SEXP getClassDef(Context context, String className) {
    return getClassTable(context).
        findVariable(context, Symbol.get(className));
  }

  private static Environment getClassTable(Context context) {
    return (Environment) getMethodsNamespaceEnv(context)
        .findVariableUnsafe(Symbol.get(".classTable")).force(context);
  }

  private static Environment getMethodsNamespaceEnv(Context context) {
    return context.getNamespaceRegistry()
        .getNamespace(context, "methods")
        .getNamespaceEnvironment();
  }

  public static AtomicVector getSuperClassesS4(Context context, String objClass) {
    SEXP containsSlot = getContainsSlot(context, objClass);
    return containsSlot.getNames();
  }

  public static SEXP computeDataClassesS4(Context context, String className) {
    Symbol argClassObjectName = Symbol.get(".__C__" + className);
    Environment environment = context.getEnvironment();
    AttributeMap map = environment.findVariable(context, argClassObjectName).force(context).getAttributes();
    return map.get("contains").getNames();
  }

}
