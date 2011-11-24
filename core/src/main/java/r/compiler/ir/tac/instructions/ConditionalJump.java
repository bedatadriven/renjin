package r.compiler.ir.tac.instructions;

import r.compiler.ir.tac.Label;
import r.compiler.ir.tac.operand.SimpleExpr;

public class ConditionalJump implements Statement {
  
  private SimpleExpr condition;
  private Label ifFalseLabel;
  
  public ConditionalJump(SimpleExpr condition, Label ifTrue) {
    this.condition = condition;
    this.ifFalseLabel = ifTrue;
  }

  public SimpleExpr getCondition() {
    return condition;
  }

  public Label getIfFalseLabel() {
    return ifFalseLabel;
  }
  
  @Override
  public String toString() {
    return "if not " + condition + " goto " + ifFalseLabel;
  }
}
