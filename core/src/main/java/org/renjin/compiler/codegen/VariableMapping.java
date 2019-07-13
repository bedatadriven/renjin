package org.renjin.compiler.codegen;

import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.ssa.SsaVariable;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;

public class VariableMapping {

  /**
   * If there is a possibility our function environment will be accessed dynamically, then
   * we need to keep all the named variables in the environment. Lower them out of SSA.
   */
  public static void lowerEnvironmentVariables(ControlFlowGraph cfg) {

    removePiFunctions(cfg);
    updateFunctions(cfg);

    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {

      for (Statement statement : basicBlock.getStatements()) {
        Expression rhs = statement.getRHS();

        if(isEnvironmentVar(rhs)) {
          statement.setChild(0, unwrapSsa(rhs));
        } else {

          for (int i = 0; i < rhs.getChildCount(); i++) {
            if (isEnvironmentVar(rhs)) {
              rhs.setChild(i, unwrapSsa(rhs.childAt(i)));
            }
          }
        }
        if(statement instanceof Assignment) {
          Assignment assignment = (Assignment) statement;
          if (isEnvironmentVar(assignment.getLHS())) {
            assignment.setLHS(unwrapSsa(assignment.getLHS()));
          }
        }
      }
    }
  }

  private static LValue unwrapSsa(Expression rhs) {
    return ((SsaVariable) rhs).getInner();
  }

  private static boolean isEnvironmentVar(Expression expr) {
    if(expr instanceof SsaVariable) {
      SsaVariable ssa = (SsaVariable) expr;
      if (ssa.getInner() instanceof EnvironmentVariable) {
        return true;
      }
    }
    return false;
  }

  public static void removePiFunctions(ControlFlowGraph cfg) {
    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      basicBlock.getStatements().removeIf(s -> {
        return s.getRHS() instanceof PiFunction;
      });
    }
  }

  public static void updateFunctions(ControlFlowGraph cfg) {
    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      for (Statement statement : basicBlock.getStatements()) {
        if(statement.getRHS() instanceof DynamicCall) {
          DynamicCall call = (DynamicCall) statement.getRHS();
          if(call.getFunctionExpr() instanceof SsaVariable) {
            SsaVariable ssa = (SsaVariable) call.getFunctionExpr();
            if(ssa.getInner() instanceof FunctionRef) {
              call.setFunctionExpr(ssa.getInner());
            }
          }
        }
      }
    }
  }
}
