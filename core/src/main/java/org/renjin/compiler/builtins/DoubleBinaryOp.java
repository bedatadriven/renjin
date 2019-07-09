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
package org.renjin.compiler.builtins;


import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.ScalarExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.FunctionCall;

import java.util.List;

/**
 * A double-precision scalar operation that can be implemented using a JVM bytecode.
 */
public class DoubleBinaryOp implements Specialization {
  
  private int opcode;
  private final ArgumentBounds x;
  private final ArgumentBounds y;
  private ValueBounds resultBounds;
  
  public DoubleBinaryOp(int opcode, ArgumentBounds x, ArgumentBounds y, ValueBounds resultBounds) {
    this.opcode = opcode;
    this.x = x;
    this.y = y;
    this.resultBounds = resultBounds;
  }

  public ValueBounds getResultBounds() {
    return resultBounds;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext, FunctionCall call, List<IRArgument> arguments) {
    return new ScalarExpr(VectorType.DOUBLE) {
      @Override
      public void loadScalar(EmitContext context, InstructionAdapter mv) {
        assert  arguments.size() == 2;
        CompiledSexp cx = x.getCompiledExpr(emitContext);
        CompiledSexp cy = y.getCompiledExpr(emitContext);

        cx.loadScalar(emitContext, mv, VectorType.DOUBLE);
        cy.loadScalar(emitContext, mv, VectorType.DOUBLE);
        mv.visitInsn(opcode);
      }
    };
  }

  public static DoubleBinaryOp trySpecialize(String name, JvmMethod overload, List<ArgumentBounds> arguments, ValueBounds resultBounds) {

    List<JvmMethod.Argument> formals = overload.getPositionalFormals();
    if(formals.size() == 2 &&
        arguments.size() == 2 &&
        formals.get(0).getClazz().equals(double.class) &&
        formals.get(1).getClazz().equals(double.class)) {

      ArgumentBounds x = arguments.get(0);
      ArgumentBounds y = arguments.get(1);

      switch (name) {
        case "+":
          return new DoubleBinaryOp(Opcodes.DADD, x, y, resultBounds);
        case "-":
          return new DoubleBinaryOp(Opcodes.DSUB, x, y, resultBounds);
        case "*":
          return new DoubleBinaryOp(Opcodes.DMUL, x, y, resultBounds);
        case "/":
          return new DoubleBinaryOp(Opcodes.DDIV, x, y, resultBounds);
      }
    }
    return null;
  }
}
