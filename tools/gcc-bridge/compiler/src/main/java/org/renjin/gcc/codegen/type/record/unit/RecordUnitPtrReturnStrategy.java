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
package org.renjin.gcc.codegen.type.record.unit;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.record.RecordLayout;
import org.renjin.repackaged.asm.Type;


public class RecordUnitPtrReturnStrategy implements ReturnStrategy {
  private RecordLayout layout;

  public RecordUnitPtrReturnStrategy(RecordLayout layout) {
    this.layout = layout;
  }

  @Override
  public Type getType() {
    return layout.getType();
  }

  @Override
  public JExpr marshall(GExpr expr) {
    return ((RecordUnitPtrExpr) expr).unwrap();
  }

  @Override
  public GExpr unmarshall(MethodGenerator mv, JExpr callExpr, TypeStrategy lhsTypeStrategy) {
    return lhsTypeStrategy.cast(mv, new RecordUnitPtrExpr(layout, callExpr));
  }

  @Override
  public JExpr getDefaultReturnValue() {
    return Expressions.nullRef(layout.getType());
  }
}
