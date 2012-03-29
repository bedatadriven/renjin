package org.renjin.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;


/**
 * Variable that is to be stored as a JVM local variable when 
 * compiled. During interpretation, it is allocated a slot
 * in the temps array.
 * 
 */
public class LocalVariable implements Variable {
  
  private final String name;
  private final int offset;
 
  public LocalVariable(String name, int offset) {
    super();
    this.name = name;
    this.offset = offset;
  }
  
  @Override
  public void setValue(Context context, Object[] temp, Object value) {
    temp[offset] = value;
  }

  @Override
  public Variable replaceVariable(Variable name, Variable newName) {
    return this.equals(name) ? newName : this;
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    return temps[offset];
  }

  @Override
  public Set<Variable> variables() {
    return Collections.<Variable>singleton(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    LocalVariable other = (LocalVariable) obj;
    
    return name.equals(other.name);
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public List<Expression> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitLocalVariable(this);
  }

  @Override
  public SEXP getSExpression() {
    throw new UnsupportedOperationException();
  } 
}
