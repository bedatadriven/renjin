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
package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.fun.FunctionRefGenerator;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.Realloc;
import org.renjin.repackaged.asm.Handle;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

/**
 * Generates calls to realloc().
 *
 * <p>The C library function void *realloc(void *ptr, size_t size) attempts to resize the memory block pointed to
 * by ptr that was previously allocated with a call to malloc or calloc. .</p>
 */
public class ReallocCallGenerator implements CallGenerator, MethodHandleGenerator {

  private TypeOracle typeOracle;

  public ReallocCallGenerator(TypeOracle typeOracle) {
    this.typeOracle = typeOracle;
  }

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {

    // If the return result is not used, then it's a no-op
    if(call.getLhs() == null) {
      return;
    }

    // Get the type of the variable we're assigning to
    GimpleType pointerType = call.getLhs().getType();

    // Get generators for the fat pointer and new length
    PtrExpr pointer = (PtrExpr) exprFactory.findGenerator(call.getOperand(0));
    JExpr size = exprFactory.findPrimitiveGenerator(call.getOperand(1));

    GExpr reallocatedPointer = pointer.realloc(mv, size);

    GExpr lhs = exprFactory.findGenerator(call.getLhs());
    lhs.store(mv, reallocatedPointer);
  }


  @Override
  public JExpr getMethodHandle() {
    return new FunctionRefGenerator(new Handle(Opcodes.H_INVOKESTATIC,
        Type.getInternalName(Realloc.class), "realloc",
        Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class), Type.INT_TYPE)));
  }
}
