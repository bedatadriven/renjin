package r.compiler.ir.tac.statements;

import java.util.Arrays;
import java.util.Set;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.SimpleExpression;
import r.compiler.ir.tac.expressions.Variable;
import r.lang.Context;
import r.lang.Logical;
import r.lang.SEXP;

public class IfStatement implements Statement, BasicBlockEndingStatement {
  
  private SimpleExpression condition;
  private IRLabel trueTarget;
  private IRLabel falseTarget;
  
  public IfStatement(SimpleExpression condition, IRLabel trueTarget, IRLabel falseTarget) {
    this.condition = condition;
    this.trueTarget = trueTarget;
    this.falseTarget = falseTarget;
  }

  public SimpleExpression getCondition() {
    return condition;
  }
  
  @Override
  public Expression getRHS() {
    return condition;
  }
  
  public IRLabel getTrueTarget() {
    return trueTarget;
  }

  public IRLabel getFalseTarget() {
    return falseTarget;
  }
  
  public IfStatement setTrueTarget(IRLabel label) {
    return new IfStatement(condition, label, falseTarget);
  }
  
  public IfStatement setFalseTarget(IRLabel label) {
    return new IfStatement(condition, trueTarget, label);
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Arrays.asList(trueTarget, falseTarget);
  }

  @Override
  public Object interpret(Context context, Object[] temp) {
    boolean conditionalValue =  toBoolean(condition.retrieveValue(context, temp));
    if(conditionalValue) {
      return trueTarget;
    } else {
      return falseTarget;
    }
  }
  
  @Override
  public Set<Variable> variables() {
    return condition.variables();
  }
  
  @Override
  public Statement withRHS(Expression newRHS) {
    if(!(newRHS instanceof SimpleExpression)) {
      throw new IllegalArgumentException("if statement requires simple rhs");
    }
    return new IfStatement((SimpleExpression) newRHS, trueTarget, falseTarget);
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

  @Override
  public String toString() {
    return "if " + condition + " goto " + trueTarget + " else " + falseTarget;
  }
}
