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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;

public interface IntExpr extends PrimitiveExpr {

  JExpr jexpr();

  GExpr bitwiseXor(GExpr operand);

  GExpr bitwiseNot();

  GExpr bitwiseAnd(GExpr operand);

  GExpr bitwiseOr(GExpr operand);

  GExpr shiftLeft(GExpr operand);

  GExpr shiftRight(GExpr operand);

  GExpr rotateLeft(GExpr operand);

  IntExpr toSignedInt(int precision);

}
