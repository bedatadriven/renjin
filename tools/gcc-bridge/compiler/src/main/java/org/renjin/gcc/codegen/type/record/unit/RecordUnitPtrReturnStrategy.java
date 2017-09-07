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
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.type.record.RecordLayout;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrStrategy;
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
    if(lhsTypeStrategy instanceof RecordUnitPtrStrategy) {
      RecordUnitPtrStrategy lhsUnitPtrStrategy = (RecordUnitPtrStrategy) lhsTypeStrategy;
      return new RecordUnitPtrExpr(layout, Expressions.cast(callExpr, lhsUnitPtrStrategy.getJvmType()));

    } else if(lhsTypeStrategy instanceof RecordClassTypeStrategy) {
      // In some cases, when you have a function like this:
      //    MyClass& do_something(MyClass& x);
      //
      // and an assignment like this:
      //
      //    MyClass x = do_something(y);
      //
      // GCC does not generate an intermediate pointer value and a mem_ref like you
      // would expect. I can't seem to reproduce this in a test case, so here is a workaround:
      RecordClassTypeStrategy lhsValueTypeStrategy = (RecordClassTypeStrategy) lhsTypeStrategy;
      return new RecordUnitPtrExpr(layout, Expressions.cast(callExpr, lhsValueTypeStrategy.getJvmType()));

    } else if(lhsTypeStrategy instanceof VoidPtrStrategy) {
      return new VoidPtrExpr(callExpr);

    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported cast from return value %s to record unit pointer [%s]", 
              lhsTypeStrategy.getClass().getName(), layout.getType()));
    }
  }

  @Override
  public JExpr getDefaultReturnValue() {
    return Expressions.nullRef(layout.getType());
  }
}
