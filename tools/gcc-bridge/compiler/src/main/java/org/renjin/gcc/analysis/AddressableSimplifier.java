package org.renjin.gcc.analysis;

import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleMemRef;
import org.renjin.gcc.gimple.expr.GimplePointerPlus;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleStatement;

public class AddressableSimplifier implements FunctionBodyTransformer {

  public static final AddressableSimplifier INSTANCE = new AddressableSimplifier();

  private AddressableSimplifier() {
  }

  @Override
  public boolean transform(TreeLogger logger, GimpleCompilationUnit unit, GimpleFunction fn) {

    boolean updated = false;

    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      for (GimpleStatement statement : basicBlock.getStatements()) {
        if(statement instanceof GimpleAssignment) {
          GimpleAssignment assignment = (GimpleAssignment) statement;

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
        }
      }
    }
    return updated;
  }
}
