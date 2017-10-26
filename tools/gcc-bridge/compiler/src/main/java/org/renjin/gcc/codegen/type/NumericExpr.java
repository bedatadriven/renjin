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
package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.complex.ComplexExpr;
import org.renjin.gcc.codegen.type.primitive.RealExpr;

/**
 * Common interface to Complex, Integer, and Real
 */
public interface NumericExpr extends GExpr {

  NumericExpr plus(GExpr operand);

  NumericExpr minus(GExpr operand);

  NumericExpr multiply(GExpr operand);

  NumericExpr divide(GExpr operand);

  NumericExpr remainder(GExpr operand);

  NumericExpr negative();

  NumericExpr min(GExpr operand);

  NumericExpr max(GExpr operand);

  NumericExpr absoluteValue();

  ComplexExpr toComplexExpr();

  RealExpr toRealExpr();
}
