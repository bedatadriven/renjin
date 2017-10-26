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
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.complex.ComplexExpr;


public abstract class AbstractIntExpr extends AbstractPrimitiveExpr implements IntExpr, NumericExpr {

  protected AbstractIntExpr(JExpr expr, GExpr address) {
    super(expr, address);
  }

  @Override
  public final NumericExpr toNumericExpr() {
    return this;
  }

  @Override
  public final IntExpr toIntExpr() {
    return this;
  }

  @Override
  public final ComplexExpr toComplexExpr() {
    return toRealExpr().toComplexExpr();
  }


  protected static JExpr bits(GExpr operand) {
    return operand.toPrimitiveExpr().toSignedInt(32).jexpr();
  }
}
