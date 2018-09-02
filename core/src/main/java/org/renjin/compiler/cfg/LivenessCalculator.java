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
package org.renjin.compiler.cfg;

import org.renjin.compiler.ir.ssa.PhiFunction;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.guava.collect.HashMultimap;
import org.renjin.repackaged.guava.collect.Multimap;

import java.util.BitSet;
import java.util.List;

public class LivenessCalculator {

  private final ControlFlowGraph cfg;
  private final UseDefMap useDefMap;

  /**
   * PhiDefs(B) denotes the set of variables defined by a φ-operation in B.
   */
  private final Multimap<BasicBlock, LValue> phiDefinitions = HashMultimap.create();

  /**
   * B (φ excluded)
   */
  private final Multimap<BasicBlock, LValue> nonPhiDefinitions = HashMultimap.create();

  public LivenessCalculator(ControlFlowGraph cfg, UseDefMap useDefMap) {
    this.cfg = cfg;
    this.useDefMap = useDefMap;

    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      for (Statement statement : basicBlock.getStatements()) {
        if(statement instanceof Assignment) {
          Assignment assignment = (Assignment) statement;
          if(assignment.getRHS() instanceof PhiFunction) {
            phiDefinitions.put(basicBlock, assignment.getLHS());
          } else {
            nonPhiDefinitions.put(basicBlock, assignment.getLHS());
          }
        }
      }
    }

  }

  private void upAndMark(BitSet liveIn, BitSet liveOut, BasicBlock b, LValue v) {
    // if def(v) ∈ B (φ excluded) return
    if(nonPhiDefinitions.containsEntry(b, v)) {
      // Killed in the block, stop
      return;
    }

    // if v ∈ LiveIn(B) then return
    if(liveIn.get(b.getIndex())) {
      // Propagation already done, stop
      return;
    }

    // LiveIn(B) = LiveIn(B) ∪ {v}
    liveIn.set(b.getIndex());

    // if v ∈ PhiDefs(B) then return . Do not propagate φ definitions
    //
    if(phiDefinitions.containsEntry(b, v)) {
      return;
    }

    // Propagate backward
    for (BasicBlock p : b.getPredecessors()) {
      liveOut.set(p.getIndex());
      upAndMark(liveIn, liveOut, p, v);
    }
  }

  public BitSet computeLiveOutSet(LValue v) {
    BitSet liveIn = new BitSet();
    BitSet liveOut = new BitSet();
    for (BasicBlock b : useDefMap.getUsedBlocks(v)) {
      if(b != cfg.getExit()) {
        if (usedAtExitOf(b, v)) {
          // Used in the φ of a successor block
          liveOut.set(b.getIndex());
        }
        upAndMark(liveIn, liveOut, b, v);
      }
    }

    System.out.println("liveIn = " + liveIn);
    System.out.println("liveOut = " + liveOut);
    return liveOut;
  }

  private boolean usedAtExitOf(BasicBlock b, LValue v) {
    List<BasicBlock> successors = b.getSuccessors();
    for (BasicBlock successor : successors) {
      for (Statement statement : successor.getStatements()) {
        if(statement instanceof Assignment) {
          if (statement.getRHS() instanceof PhiFunction) {
            PhiFunction phiFunction = (PhiFunction) statement.getRHS();
            for (Variable variable : phiFunction.getArguments()) {
              if(variable.equals(v)) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }



  /**
   * Returns true if the variable {@code v} is defined in basic block {@code b},
   * excluding any assignments from a phi statement.
   *
   *
   */
  private boolean isDefinedInExcludingPhiStatements(BasicBlock b, LValue v) {
    for (Statement statement : b.getStatements()) {
      if(statement instanceof Assignment) {

      }
    }
    return false;
  }

  /**
   *
   * @param b
   * @param v
   * @return
   */
  private boolean inPhiDefs(BasicBlock b, LValue v) {

    return false;
  }
}
