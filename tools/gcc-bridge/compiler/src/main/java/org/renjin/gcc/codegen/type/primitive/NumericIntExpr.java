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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.complex.ComplexExpr;

public interface NumericIntExpr extends IntExpr, NumericExpr {

  @Override
  NumericIntExpr plus(GExpr operand);

  @Override
  NumericIntExpr minus(GExpr operand);

  @Override
  NumericIntExpr multiply(GExpr operand);

  @Override
  NumericIntExpr absoluteValue();

  @Override
  NumericIntExpr bitwiseXor(GExpr operand);

  @Override
  NumericIntExpr bitwiseNot();

  @Override
  NumericIntExpr bitwiseAnd(GExpr operand);

  @Override
  NumericIntExpr bitwiseOr(GExpr operand);
}
