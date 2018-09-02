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
package org.renjin.compiler.codegen.var;

import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.LivenessCalculator;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Statement;

import java.util.BitSet;

public abstract class AbstractMutableVar extends VariableStrategy {

  private final LValue variable;
  private final LivenessCalculator livenessCalculator;
  private BitSet liveOut = null;


  public AbstractMutableVar(LValue variable, LivenessCalculator livenessCalculator) {
    this.variable = variable;
    this.livenessCalculator = livenessCalculator;
  }

  /**
   * @return true if this variable is "live out" at the given {@code statement}. In other words, can we
   * mutate it here without interfering with subsequent uses?
   */
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
