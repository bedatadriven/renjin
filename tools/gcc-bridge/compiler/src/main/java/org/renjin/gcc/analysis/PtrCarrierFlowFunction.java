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
package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePointerType;

import java.util.List;

/**
 * Identifies local integer variables that are actually pointer addresses.
 *
 * <p>For example, in the "triangle" library, there is a function which first casts
 * a pointer to an unsigned long value, and then uses integer arithmetic to align the offset
 * to a multiple of the desired alignment, before then casting it back to a pointer.
 *
 * <p>We need to tag these "integer" variables as actually holding an offset relative to a specific
 * VPtr, so that when we </p>
 *
 */
public class PtrCarrierFlowFunction implements FlowFunction<VarSet> {
  @Override
  public VarSet initialState() {
    return new VarSet();
  }

  @Override
  public VarSet transfer(VarSet entryState, Iterable<GimpleStatement> basicBlock) {
    VarSet pointerCarriers = new VarSet(entryState);
    for (GimpleStatement statement : basicBlock) {
      if(statement instanceof GimpleAssignment) {
        GimpleAssignment assignment = (GimpleAssignment) statement;
        if(isLhsIntegerVariable(assignment)) {
          GimpleVariableRef lhs = (GimpleVariableRef) assignment.getLHS();
          if(isRhsPointer(assignment)) {
            pointerCarriers.add(lhs);
          }
          if(isRhsPointerCarryingIntegerOperation(pointerCarriers, assignment)) {
            pointerCarriers.add(lhs);
          }
        }
      }
    }

    return pointerCarriers;
  }



  private boolean isLhsIntegerVariable(GimpleAssignment assignment) {
    GimpleLValue lhs = assignment.getLHS();
    return
        lhs instanceof GimpleVariableRef &&
        lhs.getType() instanceof GimpleIntegerType;
  }

  private boolean isRhsPointer(GimpleAssignment assignment) {
    switch (assignment.getOperator()) {
      case POINTER_PLUS_EXPR:
        return true;
      case VAR_DECL:
      case PARM_DECL:
      case NOP_EXPR:
        return assignment.getOperands().get(0).getType() instanceof GimplePointerType;

      default:
        return false;
    }
  }

  private boolean isRhsPointerCarryingIntegerOperation(VarSet pointerCarriers, GimpleAssignment assignment) {
    return isOffsetManipulationOperation(assignment) &&
           isAnyOperandCarryingAPointer(pointerCarriers, assignment);
  }

  private boolean isAnyOperandCarryingAPointer(VarSet pointerCarriers, GimpleAssignment assignment) {
    for (GimpleExpr operand : assignment.getOperands()) {
      if(pointerCarriers.contains(operand)) {
        return true;
      }
    }
    return false;
  }

  private boolean isOffsetManipulationOperation(GimpleAssignment assignment) {
    switch (assignment.getOperator()) {

      /* Some C code use +/- to align pointers to specific boundaries */
      case PLUS_EXPR:
      case MINUS_EXPR:
        return true;

      /* ... and other code "cleverly" stashes extra information temporarily in pointers... 😱 */
      /* See this comment from the triangle library: */

      /*    encode() compresses an oriented triangle into a single pointer.  It       */
      /*   relies on the assumption that all triangles are aligned to four-byte    */
      /*   boundaries, so the two least significant bits of (otri).tri are zero.   */
      case BIT_IOR_EXPR:
      case BIT_AND_EXPR:
      case BIT_XOR_EXPR:
        return true;
      default:
        return false;
    }
  }

  @Override
  public VarSet join(List<VarSet> inputs) {
    return VarSet.unionAll(inputs);
  }
}
