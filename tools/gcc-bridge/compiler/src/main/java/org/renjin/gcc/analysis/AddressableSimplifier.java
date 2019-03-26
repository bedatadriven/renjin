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
package org.renjin.gcc.analysis;

import org.renjin.gcc.logging.LogManager;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleMemRef;
import org.renjin.gcc.gimple.expr.GimplePointerPlus;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Simplifies redundant Gimple address of expressions such as {@code &*x} to {@code x}.
 */
public class AddressableSimplifier implements FunctionBodyTransformer {

  public static final AddressableSimplifier INSTANCE = new AddressableSimplifier();

  private AddressableSimplifier() {
  }

  @Override
  public boolean transform(LogManager logManager, GimpleCompilationUnit unit, GimpleFunction fn) {

    boolean updated = false;

    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      for (GimpleStatement statement : basicBlock.getStatements()) {
        if(statement instanceof GimpleAssignment) {
          if(fixAssignment((GimpleAssignment) statement)) {
            updated = true;
          }
        } else if(statement instanceof GimpleCall) {
          if(fixCallArguments((GimpleCall) statement)) {
            updated = true;
          }
        }
      }
    }
    return updated;
  }


  private boolean fixAssignment(GimpleAssignment assignment) {
    boolean updated = false;

    // Simplify &*x to x

    if(assignment.getOperator() == GimpleOp.ADDR_EXPR) {
      GimpleAddressOf addressOf = (GimpleAddressOf) assignment.getOperands().get(0);
      if(addressOf.getValue() instanceof GimpleMemRef) {
        GimpleMemRef memRef = (GimpleMemRef) addressOf.getValue();

        if(memRef.isOffsetZero()) {
          // &*x => x
          assignment.setOperator(GimpleOp.NOP_EXPR);
          assignment.getOperands().set(0, memRef.getPointer());
        } else {
          // &*(x+8) => x+8
          GimpleExpr offset = memRef.getOffset();
          offset.setType(memRef.getPointer().getType());

          assignment.setOperator(GimpleOp.POINTER_PLUS_EXPR);
          assignment.getOperands().set(0, memRef.getPointer());
          assignment.getOperands().add(offset);
        }
        updated = true;
      }
    }

    // Fix types in *a = *b
    // which sometimes have types (int8[] = int8[])
    if( assignment.getLHS() instanceof GimpleMemRef &&
        assignment.getOperator() == GimpleOp.MEM_REF) {

      GimpleMemRef lhs = (GimpleMemRef) assignment.getLHS();
      GimpleMemRef rhs = (GimpleMemRef) assignment.getOperands().get(0);

      if( isByteArray(lhs.getType()) &&
          isByteArray(rhs.getType())) {

        if (lhs.isOffsetZero() && rhs.isOffsetZero()) {
          GimpleIndirectType lhsType = (GimpleIndirectType) lhs.getPointer().getType();
          GimpleIndirectType rhsType = (GimpleIndirectType) rhs.getPointer().getType();

          lhs.setType(lhsType.getBaseType());
          rhs.setType(rhsType.getBaseType());
        }
      }
    }
    return updated;
  }

  private boolean fixCallArguments(GimpleCall statement) {
    boolean updated = false;
    List<GimpleExpr> operands = statement.getOperands();
    for (int i = 0; i < operands.size(); i++) {
      GimpleExpr op = operands.get(i);
      if (op instanceof GimpleAddressOf) {
        GimpleAddressOf addressOf = (GimpleAddressOf) op;
        if (addressOf.getValue() instanceof GimpleMemRef) {
          GimpleMemRef memRef = (GimpleMemRef) addressOf.getValue();
          if (memRef.isOffsetZero()) {
            statement.setOperand(i, memRef.getPointer());
          } else {
            statement.setOperand(i, new GimplePointerPlus(memRef.getPointer(), memRef.getOffset()));
          }
          updated = true;
        }
      }
    }
    return updated;
  }


  private boolean isByteArray(GimpleType type) {
    if(type instanceof GimpleArrayType) {
      GimpleArrayType arrayType = (GimpleArrayType) type;
      if(arrayType.getComponentType() instanceof GimpleIntegerType) {
        GimpleIntegerType integerType = (GimpleIntegerType) arrayType.getComponentType();
        if(integerType.getPrecision() == 8) {
          return true;
        }
      }
    }
    return false;
  }
}
