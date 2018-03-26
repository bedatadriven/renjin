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
import org.renjin.methods.Methods;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.*;

import java.util.*;

import static org.renjin.methods.MethodDispatch.*;

public class S4 {

  private static final String PREFIX_CLASS = ".__C__";


  /**
   * Attempts to dispatch to an S4 method based on the calling arguments.
   *
   * <p>If a suitable method is found, it is evaluated and this method returns the result of the function call</p>
   *
   * <p>If no method is found, then this method returns {@code null}</p>
   */
  public static SEXP tryS4DispatchFromPrimitive(@Current Context context, SEXP source, PairList args,
                                                Environment rho, String group, String opName) {

    Generic generic = Generic.primitive(opName, group);
    MethodLookupTable lookupTable = new MethodLookupTable(generic, context);
    if(lookupTable.isEmpty()) {
      return null;
    }

    CallingArguments arguments = CallingArguments.primitiveArguments(context, rho, lookupTable.getArgumentMatcher(), source, args);
    S4ClassCache classCache = new S4ClassCache(context);
    DistanceCalculator calculator = new DistanceCalculator(classCache);

    RankedMethod selectedMethod = lookupTable.selectMethod(arguments, calculator);
    if(selectedMethod == null) {
      return null;
    }

    Closure function = selectedMethod.getMethodDefinition();

    PairList.Builder coercedArgs = Methods.coerceArguments(context, arguments, classCache, selectedMethod);

    if (dispatchWithoutMeta(opName, source, selectedMethod)) {
      FunctionCall call = new FunctionCall(function, coercedArgs.build());
      return context.evaluate(call);
      
    } else {
      Map<Symbol, SEXP> metadata = generateCallMetaData(context, selectedMethod, arguments, opName);
      FunctionCall call = new FunctionCall(function, arguments.getPromisedArgs());
      return ClosureDispatcher.apply(context, rho, call, function, coercedArgs.build(), metadata);
    }
  }

  private static boolean dispatchWithoutMeta(String opName, SEXP source, RankedMethod rank) {
    boolean hasS3Class = source.getAttribute(Symbol.get(".S3Class")).length() != 0;
    boolean genericExact = !rank.getMethod().isGroupGeneric() && rank.isExact();
    return (!opName.contains("<-") && (genericExact || hasS3Class));
  }

  public static Map<Symbol, SEXP> generateCallMetaData(Context context, RankedMethod method, CallingArguments arguments, String opName) {
    Map<Symbol, SEXP> metadata = new HashMap<>();

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

    metadata.put(R_dot_defined, buildDotDefined(context, method));
    metadata.put(R_dot_target, buildDotTarget(method));
    metadata.put(DOT_GENERIC, method.getMethod().getGeneric().asSEXP());
    metadata.put(R_dot_Method, method.getMethodDefinition());
    if(Primitives.isPrimitive(opName)) {
      metadata.put(s_dot_Methods, Symbol.get(".Primitive(\"" + opName + "\")"));
    } else {
      metadata.put(s_dot_Methods, Null.INSTANCE);
    }
    return metadata;
  }

  private static SEXP buildDotTarget(RankedMethod method) {

    List<String> argumentClasses = method.getMethod().getSignature().getClasses();
    List<String> argumentPackages = new ArrayList<String>(Collections.nCopies(argumentClasses.size(), "methods"));

    return new StringVector.Builder()
      .addAll(argumentClasses)
      .setAttribute("names", method.getMethod().getFormalNames())
      .setAttribute("package", new StringArrayVector(argumentPackages))
      .setAttribute("class", signatureClass())
      .build();
  }

  private static SEXP buildDotDefined(Context context, RankedMethod method) {

    List<String> argumentClasses = method.getMethod().getSignature().getClasses();
    List<String> argumentPackages = new ArrayList<>();

    for (String argumentClass : argumentClasses) {
      argumentPackages.add(getClassPackage(context, argumentClass));
    }

    return new StringVector.Builder()
      .addAll(argumentClasses)
      .setAttribute("names", method.getMethod().getFormalNames())
      .setAttribute("package", new StringArrayVector(argumentPackages))
      .setAttribute("class", signatureClass())
      .build();
  }

  private static SEXP signatureClass() {
    return StringVector.valueOf("signature")
      .setAttribute("package", StringVector.valueOf("methods"));
  }

  public static String getClassPackage(Context context, String objClass) {
    if("ANY".equals(objClass) || "signature".equals(objClass)) {
      return "methods";
    }
    Environment methodsEnv = context.getNamespaceRegistry()
        .getNamespace(context, "methods")
        .getNamespaceEnvironment();

    Environment classTable = (Environment) methodsEnv
        .findVariableUnsafe(Symbol.get(".classTable")).force(context);

    SEXP classDef = classTable
        .findVariable(context, Symbol.get(objClass));

    StringArrayVector packageSlot = (StringArrayVector) classDef.getAttribute(S4Class.PACKAGE);
    return packageSlot.getElementAsString(0);
  }

  public static AtomicVector getSuperClassesS4(Context context, String objClass) {
    Environment methodsEnv = context.getNamespaceRegistry()
        .getNamespace(context, "methods")
        .getNamespaceEnvironment();

    Environment classTable = (Environment) methodsEnv
        .findVariableUnsafe(Symbol.get(".classTable")).force(context);

    SEXP classDef = classTable
        .findVariable(context, Symbol.get(objClass));

    SEXP containsSlot = classDef.getAttribute(S4Class.CONTAINS);
    return containsSlot.getNames();
  }

  public static SEXP computeDataClassesS4(Context context, String className) {
    Symbol argClassObjectName = Symbol.get(PREFIX_CLASS + className);
    Environment environment = context.getEnvironment();
    AttributeMap map = environment.findVariable(context, argClassObjectName).force(context).getAttributes();
    return map.get("contains").getNames();
  }

  public static Symbol classNameMetadata(String className) {
    return Symbol.get(PREFIX_CLASS + className);
  }
}
