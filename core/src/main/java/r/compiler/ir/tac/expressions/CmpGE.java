package r.compiler.ir.tac.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import r.lang.Context;
import r.lang.Vector;

public class CmpGE implements SimpleExpression {
  private Expression op1;
  private Expression op2;
  
  public CmpGE(Expression op1, Expression op2) {
    super();
    this.op1 = op1;
    this.op2 = op2;
  }

  @Override
  public String toString() {
    return op1 + " >= " + op2;
  }

  public Expression getOp1() {
    return op1;
  }

  public Expression getOp2() {
    return op2;
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    Integer a = (Integer)op1.retrieveValue(context, temps);
    Vector b = (Vector)op2.retrieveValue(context, temps);
    
    return a >= b.getElementAsInt(0);
  }

  @Override
  public Set<Variable> variables() {
    return Sets.union(op1.variables(), op2.variables());
  }

  @Override
  public CmpGE replaceVariable(Variable name, Variable newName) {
    return new CmpGE(op1.replaceVariable(name, newName), 
                     op2.replaceVariable(name, newName));
  }

  @Override
  public List<Expression> getChildren() {
    return Arrays.asList(op1, op2);
  }

  @Override
  public void setChild(int i, Expression expr) {
    if(i==0) {
      op1 = expr;
    } else if(i==1) {
      op2 = expr;
    } else {
      throw new IllegalArgumentException("i=" + i);
    }
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitCmpGE(this);
  }
}
