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
package org.renjin.s4;

import org.renjin.eval.Context;
import org.renjin.eval.DispatchTable;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.methods.Methods;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public final class S4 {

  private static final String R_methods = "methods";
  private static final String R_package = "package";

  public static final String CLASS_PREFIX = ".__C__";
  public static final String METHOD_PREFIX = ".__T__";
  public static final Symbol CONTAINS = Symbol.get("contains");
  public static final Symbol SUBCLASSES = Symbol.get("subclasses");
  public static final Symbol DISTANCE = Symbol.get("distance");
  public static final Symbol COERCE = Symbol.get("coerce");
  public static final Symbol REPLACE = Symbol.get("replace");
  public static final Symbol BY = Symbol.get("by");
  public static final Symbol SIMPLE = Symbol.get("simple");
  public static final Symbol TEST = Symbol.get("test");
  public static final Symbol PACKAGE = Symbol.get(R_package);
  public static final Symbol GROUP = Symbol.get("group");


  private S4() {}


  /**
   * Attempts to dispatch to an S4 method based on the calling arguments.
   *
   * <p>If a suitable method is found, it is evaluated and this method returns the result of the function call</p>
   *
   * <p>If no method is found, then this method returns {@code null}</p>
   */
  public static SEXP tryS4DispatchFromPrimitive(Context context, Environment rho,
                                                String opName,
                                                String group,
                                                FunctionCall originalCall,
                                                String[] argumentNames,
                                                SEXP[] promisedArguments) {

    Generic generic;
    if(group == null || opName.equals("!")) {
      generic = Generic.primitive(opName, new ArrayList<>());
    } else {
      generic = Generic.primitive(opName, Collections.singletonList(group));
    }

    S4MethodCache methodCache = context.getSession().getS4Cache().getS4MethodCache();
    S4MethodTable methodTable = methodCache.getMethod(context, generic, opName);

    if(methodTable == null || methodTable.isEmpty()) {
      return null;
    }

    SEXP source = promisedArguments[0].force(context);

    CallingArguments arguments = CallingArguments.primitiveArguments(context, rho, methodTable.getArgumentMatcher(),
        source, argumentNames, promisedArguments);

    Signature signature = arguments.getSignature(methodTable.getMaximumSignatureLength());

    boolean[] useInheritance = new boolean[methodTable.getMaximumSignatureLength()];
    Arrays.fill(useInheritance, Boolean.TRUE);

    RankedMethod selectedMethod = methodTable.selectMethod(context, generic, signature, useInheritance);

    if(selectedMethod == null) {
      return null;
    }

    Closure function = selectedMethod.getMethodDefinition();

    Methods.coerce(context, arguments, selectedMethod);

    FunctionCall call = new FunctionCall(function, originalCall.getArguments());

    DispatchTable dispatchTable;
    if (dispatchWithoutMeta(opName, source, selectedMethod)) {
      dispatchTable = null;
    } else {
      dispatchTable = generateCallMetaData(context, selectedMethod, signature, opName);
    }

    return function.applyPromised(context, rho, call, arguments.getArgumentNames(), arguments.getPromisedArguments(), dispatchTable);
  }

  private static boolean dispatchWithoutMeta(String opName, SEXP source, RankedMethod rank) {
    boolean hasS3Class = source.getAttribute(Symbol.get(".S3Class")).length() != 0;
    boolean genericExact = !rank.getMethod().isGroupGeneric() && rank.isExact();
    return (!opName.contains("<-") && (genericExact || hasS3Class));
  }

  public static DispatchTable generateCallMetaData(Context context, RankedMethod method, Signature signature, String opName) {

    /*
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

    return new S4DispatchMetadata(context, opName, method, signature);

  }
  public static AtomicVector computeDataClassesS4(Context context, String objClass) {
    S4Class s4Class = context.getSession()
        .getS4Cache()
        .getS4ClassCache()
        .lookupClass(context, objClass);

    if(s4Class != null) {
      return s4Class.getDefinition()
          .getAttribute(S4.CONTAINS)
          .getNames();
    } else {
      return null;
    }
  }


  public static Symbol classNameMetadata(String className) {
    return Symbol.get(CLASS_PREFIX + className);
  }

  @Internal
  public static void invalidateS4Cache(@Current Context context, String msg) {
    context.getSession().getS4Cache().invalidate();
  }

  @Internal
  public static void invalidateS4MethodCache(@Current Context context, String msg) {
    context.getSession().getS4Cache().invalidateMethodCache();
  }
}
