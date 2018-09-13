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
package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.ScalarExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.ListShape;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;

import java.util.List;

public class ListElement implements Specialization {

  private final ArgumentBounds object;
  private final int elementIndex;
  private final ValueBounds elementBounds;

  public ListElement(ArgumentBounds object, int elementIndex, ValueBounds elementBounds) {
    this.object = object;
    this.elementIndex = elementIndex;
    this.elementBounds = elementBounds;
  }

  public static Specialization trySpecialize(ArgumentBounds object, ArgumentBounds name) {
    if(object.getTypeSet() != TypeSet.LIST) {
      return null;
    }
    ListShape shape = object.getBounds().getShape();
    if(shape == null) {
      return null;
    }
    if(!name.getBounds().isConstant()) {
      return null;
    }
    SEXP constantName = name.getBounds().getConstantValue();
    if (!(constantName instanceof StringVector)) {
      return null;
    }

    String elementName = ((StringVector) constantName).getElementAsString(0);
    int elementIndex = shape.getElementIndex(elementName);
    if(elementIndex == -1) {
      return new ConstantCall(Null.INSTANCE);
    }

    return new ListElement(object, elementIndex, shape.getElementBounds(elementIndex));
  }

  @Override
  public ValueBounds getResultBounds() {
    return elementBounds;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext, List<IRArgument> arguments) {
    return object.getCompiledExpr(emitContext).elementAt(emitContext, new ScalarExpr(VectorType.INT) {
      @Override
      public void loadScalar(EmitContext context, InstructionAdapter mv) {
        mv.visitLdcInsn(elementIndex);
      }
    });
  }
}
