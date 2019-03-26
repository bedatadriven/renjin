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
package org.renjin.compiler.codegen;

import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.codegen.var.LocalVarAllocator;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.expressions.Temp;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.SEXP;

public interface EmitContext {

  /**
   * @return the index of the local variable holding the {@code Context} instance
   */
  int getContextVarIndex();

  /**
   * @return the index of the local variable holding the {@code Environment} instance
   */
  int getEnvironmentVarIndex();

  LocalVarAllocator getLocalVarAllocator();

  Label getBytecodeLabel(IRLabel label);

  void writeReturn(InstructionAdapter mv, CompiledSexp returnExpr);

  /**
   * Writes any additional instructions required at the end of this function. Or nothing if nothing is required.
   */
  void writeDone(InstructionAdapter mv);

  CompiledSexp getParamExpr(int parameterIndex);

  VariableStrategy getVariable(LValue lhs);

  default boolean isSafelyMutable(Statement statement, Expression expression) {
    if(expression instanceof LValue) {
      if(expression instanceof Temp) {
        return true;
      } else {
        VariableStrategy variable = getVariable((LValue) expression);
        return variable.isLiveOut(statement);
      }
    } else {
      return false;
    }
  }

  default CompiledSexp constantSexp(SEXP sexp) {
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        ConstantBytecode.pushConstant(mv, sexp);
      }
    };
  }
}
