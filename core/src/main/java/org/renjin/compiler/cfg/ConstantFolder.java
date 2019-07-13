package org.renjin.compiler.cfg;

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.ssa.PhiFunction;
import org.renjin.compiler.ir.ssa.SsaVariable;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.sexp.PrimitiveFunction;
import org.renjin.sexp.SEXP;

public class ConstantFolder {

  private boolean retainEnvironment = true;
  private final ControlFlowGraph cfg;

  public ConstantFolder(ControlFlowGraph cfg) {
    this.cfg = cfg;
  }

  public void fold() {
    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      if (basicBlock.isLive()) {
        for (Statement statement : basicBlock.getStatements()) {
          foldStatement(statement);
        }
      }
    }
  }

  private void foldStatement(Statement statement) {

    Expression rhs = statement.getRHS();

    // PhiFunctions are handled specially during the DeadCodeElimination phase
    if(rhs instanceof PhiFunction) {
      return;
    }

    for (int i = 0; i < rhs.getChildCount(); i++) {
      if(isReadSafeToRemove(rhs.childAt(i))) {
        ValueBounds childBounds = rhs.childAt(i).getValueBounds();
        if (childBounds.isConstant()) {
          SEXP constantValue = childBounds.getConstantValue();

          if (rhs instanceof DynamicCall && i == 0) {
            maybeFoldFunction((DynamicCall) rhs, constantValue);
          } else {
            rhs.setChild(i, new Constant(constantValue));
          }
        }
      }
    }
  }

  private boolean isReadSafeToRemove(Expression expr) {
    if(retainEnvironment) {
      if (expr instanceof SsaVariable) {
        SsaVariable ssa = (SsaVariable) expr;
        if (ssa.getInner() instanceof EnvironmentVariable) {
          return false;
        }
      }
    }
    return expr.isPure();
  }

  private static void maybeFoldFunction(DynamicCall call, SEXP constantValue) {
    if(constantValue instanceof PrimitiveFunction) {
      call.setChild(0, new BuiltinRef((PrimitiveFunction) constantValue));
    }
  }
}
