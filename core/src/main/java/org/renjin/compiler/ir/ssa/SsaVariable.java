package org.renjin.compiler.ir.ssa;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.renjin.compiler.ir.IRUtils;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.ExpressionVisitor;
import org.renjin.compiler.ir.tac.expressions.SimpleExpression;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;


public class SsaVariable implements Variable {
  private final Variable inner;
  private final int version;
  
  public SsaVariable(Variable inner, int version) {
    super();
    if(inner instanceof SsaVariable) {
      throw new IllegalArgumentException("SSA variables should not be nested");
    }
    this.inner = inner;
    this.version = version;
  }
  
  public Variable getInner() {
    return inner;
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    return inner.retrieveValue(context, temps);
  }
  
  @Override
  public Set<Variable> variables() {
    return Collections.<Variable>singleton(this);
  }
  
  @Override
  public Variable replaceVariable(Variable name, Variable newName) {
    return this.equals(name) ? newName : this;
  }
  
  @Override
  public void setValue(Context context, Object[] temp, Object value) {
    inner.setValue(context, temp, value);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(inner.toString());
    
    IRUtils.appendSubscript(sb, version);
    
    return sb.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + inner.hashCode();
    result = prime * result + version;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SsaVariable other = (SsaVariable) obj;
    return inner.equals(other.inner) && version == other.version;
  }

  @Override
  public List<Expression> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException();
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitSsaVariable(this);
  }

  @Override
  public SEXP getSExpression() {
    return inner.getSExpression();
  }

}
