/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.compiler.codegen.var;

import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.LivenessCalculator;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.ArrayExpr;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.BitSet;

/**
 * Store an S-expression known to be an atomic vector as a java array local variable.
 * the type of the vector must be known
 */
public class ArrayVar extends VariableStrategy {

  private final LValue variable;
  private final VectorType vectorType;
  private final int index;
  private final LivenessCalculator livenessCalculator;

  private BitSet liveOut = null;

  public ArrayVar(LValue variable, LivenessCalculator livenessCalculator, LocalVarAllocator localVars, ValueBounds bounds) {
    this.variable = variable;
    this.livenessCalculator = livenessCalculator;
    this.index = localVars.reserveArray();
    this.vectorType = VectorType.of(bounds.getTypeSet());
  }

  @Override
  public CompiledSexp getCompiledExpr() {
    return new ArrayExpr(vectorType) {
      @Override
      public void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType) {
        if(vectorType != ArrayVar.this.vectorType) {
          throw new UnsupportedOperationException("TODO: Array type mismatch");
        }
        mv.visitVarInsn(Opcodes.ALOAD, index);
      }
    };
  }

  @Override
  public void store(EmitContext emitContext, InstructionAdapter mv, CompiledSexp compiledSexp) {
    compiledSexp.loadArray(emitContext, mv, vectorType);
    mv.visitVarInsn(Opcodes.ASTORE, index);
  }

  @Override
  public boolean isLiveOut(Statement statement) {

    // Check first to see if it is used in this block
    BasicBlock basicBlock = statement.getBasicBlock();
    int useIndex = basicBlock.getStatements().indexOf(statement);

    for (int i = useIndex + 1; i < basicBlock.getStatements().size(); i++) {
      Expression rhs = basicBlock.getStatements().get(i).getRHS();
      for (int j = 0; j < rhs.getChildCount(); j++) {
        if(rhs.childAt(j).equals(variable)) {
          return true;
        }
      }
    }

    if(liveOut == null) {
      liveOut = livenessCalculator.computeLiveOutSet(variable);
    }
    return !liveOut.get(basicBlock.getIndex());
  }
}
