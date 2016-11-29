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
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.util.Collections;
import java.util.List;


class RecordUnitPtrParam implements ParamStrategy {

  private RecordUnitPtrStrategy strategy;

  public RecordUnitPtrParam(RecordUnitPtrStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(strategy.getJvmType());
  }

  @Override
  public GExpr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<JLValue> paramVars, VarAllocator localVars) {
    if(parameter.isAddressable()) {
      throw new UnsupportedOperationException("TODO: Addressable parameters");
    }

    return new RecordUnitPtr(paramVars.get(0));
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {

    if(!argument.isPresent()) {
      mv.visitInsn(Opcodes.ACONST_NULL);
      return;
    }


    GExpr expr = argument.get();
    if(expr instanceof RecordUnitPtr) {
      Expressions.cast(((RecordUnitPtr) expr).unwrap(), strategy.getJvmType()).load(mv);

    } else if(expr instanceof VoidPtr) {
      ((VoidPtr) expr).unwrap().load(mv);
      mv.visitLdcInsn(strategy.getJvmType());
      mv.invokestatic(ObjectPtr.class, "castUnit",
          Type.getMethodDescriptor(Type.getType(Object.class),
              Type.getType(Object.class), Type.getType(Class.class)));

      mv.checkcast(strategy.getJvmType());

    } else {
      throw new UnsupportedOperationException("Cannot pass expression of type " + expr.getClass().getName() +
          " as a record unit pointer of type " + strategy.getJvmType());
    }

  }
}
