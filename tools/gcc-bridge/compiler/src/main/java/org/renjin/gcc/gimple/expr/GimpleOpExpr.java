package org.renjin.gcc.gimple.expr;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import org.renjin.gcc.gimple.GimpleOp;

import java.util.List;

/**
 * Nested expression introduced by the tree building phase
 */
public class GimpleOpExpr extends GimpleExpr {
  
  private GimpleOp op;
  private List<GimpleExpr> operands;

  public GimpleOpExpr(GimpleOp op, List<GimpleExpr> operands) {
    this.op = op;
    this.operands = operands;
  }

  public GimpleOp getOp() {
    return op;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findOrDescend(operands, predicate, results);
  }

  @Override
  public boolean replace(Predicate<? super GimpleExpr> predicate, GimpleExpr replacement) {
    for (int i = 0; i < operands.size(); i++) {
      if(predicate.apply(operands.get(i))) {
        operands.set(i, replacement);
        return true;
      }
    }
    return false;
  }

  public List<GimpleExpr> getOperands() {
    return operands;
  }

  @Override
  public String toString() {
    return "gimple_op<" + op + ", " + Joiner.on(", ").join(operands) + ">";
  }
}
