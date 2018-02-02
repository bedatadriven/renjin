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

import org.renjin.eval.ArgumentMatcher;
import org.renjin.eval.Session;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.sexp.*;

import java.util.*;

/**
 * A table containing all of the Methods defined for a given {@link Generic}
 */
public class MethodLookupTable {

  private Generic generic;
  private ArgumentMatcher argumentMatcher;

  private List<Method> methods = new ArrayList<>();

  /**
   * Maps a signature string (for example, "integer#ANY#integer") to the method definition.
   */
  private Map<String, Method> signatureMap = new HashMap<>();



  private int maximumSignatureLength = 0;

  public MethodLookupTable(Generic generic, Session session) {
    this.generic = generic;

    List<Frame> namespaceFrames = new ArrayList<>();
    namespaceFrames.add(session.getGlobalEnvironment().getFrame());

    for (Namespace namespace : session.getNamespaceRegistry().getLoadedNamespaces()) {
      namespaceFrames.add(namespace.getNamespaceEnvironment().getFrame());
    }

    for (Frame namespaceFrame : namespaceFrames) {
      SEXP table = namespaceFrame.getVariable(methodTableName(generic));
      if (table != Symbol.UNBOUND_VALUE) {
        addMethods((Environment) table, 0);
      }
      for (int i = 0; i < generic.getGroups().size(); i++) {
        SEXP groupTable = namespaceFrame.getVariable(methodTableName(generic, generic.getGroups().get(i)));
        if (groupTable != Symbol.UNBOUND_VALUE) {
          addMethods((Environment) groupTable, i + 1);
        }
      }
    }

    // TODO: is this really the best way to find the formals of the generic?
    if(signatureMap.isEmpty()) {
      this.argumentMatcher = new ArgumentMatcher(Null.INSTANCE);
    } else {
      // Each method definition in our table contains the original formals, so just
      // choose an arbitrary method definition
      Method method = signatureMap.values().iterator().next();
      this.argumentMatcher = new ArgumentMatcher(method.getDefinition().getFormals());
    }
  }

  /**
   * Add methods listed in a namespace's method table. The table is an environment
   * where each defined method is bound to its signature. For example:
   *
   * <pre>
   * > ls(envir=getNamespace("methods")[[".__T__show:methods"]])
   * [1] "ANY"                       "AtomicList"
   * [3] "classGeneratorFunction"    "classRepresentation"
   * [5] "DataTable"                 "Dups"
   * [7] "envRefClass"               "externalptr"
   * [9] "externalRefMethod"         "FilterClosure"
   * [11] "FilterMatrix"              "genericFunction"
   * [13] "genericFunctionWithTrace"  "GenomeDescription"
   * [15] "GroupedIRanges"            "Grouping"
   * [17] "Hits"                      "IPos"
   *
   * </pre>
   */
  private void addMethods(Environment table, int groupLevel) {

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

    for (NamedValue namedValue : table.namedValues()) {
      String signature = namedValue.getName();
      SEXP definition = namedValue.getValue();
      Method method = new Method(generic, groupLevel, signature, definition);

      if(method.getSignatureLength() > maximumSignatureLength) {
        maximumSignatureLength = method.getSignatureLength();
      }

      methods.add(method);
      signatureMap.put(signature, method);
    }
  }

  private Symbol methodTableName(Generic generic) {
    return Symbol.get(".__T__" + generic.getName() + ":" + generic.getPackageName());
  }


  private Symbol methodTableName(Generic generic, String groupName) {
    return Symbol.get(".__T__" + groupName + ":" + generic.getPackageName());
  }

  /**
   *
   * @return {@code true} if there are no methods defined for this Generic.
   */
  public boolean isEmpty() {
    return signatureMap.isEmpty();
  }

  /**
   * @return a matcher for this Generic's arguments.
   */
  public ArgumentMatcher getArgumentMatcher() {
    return argumentMatcher;
  }

  public int getMaximumSignatureLength() {
    return maximumSignatureLength;
  }


  public RankedMethod selectMethod(CallingArguments arguments, DistanceCalculator distanceCalculator) {

    Signature callingSignature = arguments.getSignature(maximumSignatureLength);

    RankedMethod bestMatch = null;

    for (Method method : methods) {
      RankedMethod rankedMethod = new RankedMethod(method, callingSignature, distanceCalculator);
      if(rankedMethod.isCandidate()) {
        if (bestMatch == null || rankedMethod.isBetterThan(bestMatch)) {
          bestMatch = rankedMethod;
        }
      }
    }

    return bestMatch;
  }
}
