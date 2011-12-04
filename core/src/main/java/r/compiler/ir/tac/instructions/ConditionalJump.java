package r.compiler.ir.tac.instructions;

import r.compiler.ir.tac.Label;
import r.compiler.ir.tac.operand.SimpleExpr;
import r.lang.Context;
import r.lang.Logical;
import r.lang.SEXP;

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

  @Override
  public Object interpret(Context context, Object[] temp) {
    boolean conditionalValue =  toBoolean(condition.retrieveValue(context, temp));
    if(!conditionalValue) {
      return ifFalseLabel;
    }
   
    return null;
  }
  
  private boolean toBoolean(Object obj) {
    if(obj instanceof Boolean) {
      return (Boolean)obj;
    } else if(obj instanceof SEXP) {
      return ((SEXP)obj).asLogical() == Logical.TRUE;
    } else {
      throw new IllegalArgumentException(""+obj);
    }
  }
}
