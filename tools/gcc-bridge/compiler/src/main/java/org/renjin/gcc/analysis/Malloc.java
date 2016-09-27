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
package org.renjin.gcc.analysis;

import org.renjin.gcc.codegen.cpp.CppStandardLibrary;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;

/**
 * Predicates for malloc statements
 */
public class Malloc {

  public static boolean isMalloc(GimpleExpr functionExpr) {
    return isFunctionNamed(functionExpr, "malloc")  ||
           isFunctionNamed(functionExpr, "alloca") ||
           isFunctionNamed(functionExpr, "realloc") || 
           isFunctionNamed(functionExpr, "__builtin_malloc") ||
           isFunctionNamed(functionExpr, CppStandardLibrary.NEW_OPERATOR);
  }

  private static boolean isFunctionNamed(GimpleExpr functionExpr, String name) {
    if (functionExpr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) functionExpr;
      if (addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
        return ref.getName().equals(name);
      }
    }
    return false;
  }

}
