package org.renjin.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.renjin.compiler.ir.IRUtils;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;


/**
 * A slot for a temporary value. 
 * A temporary value can only be assigned once, so it is not
 * have to be processed by the SSA transformation
 */
public class Temp implements LValue {
  private static final String TAO = "\u03C4";
  private final int index;
  
  public Temp(int index) {
    this.index = index;
  }
  
  public int getIndex() {
    return index;
  }
  
  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    return temps[index];
  }
  
  @Override
  public void setValue(Context context, Object[] temp, Object value) {
    temp[index] = value; 
  }

  @Override 
  public String toString() {
    StringBuilder sb = new StringBuilder(TAO);
    IRUtils.appendSubscript(sb, index+1);
    return sb.toString();
  }

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }

  @Override
  public void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public Temp replaceVariable(Variable name, Variable newName) {
    return this;
  }

  @Override
  public int hashCode() {
    return index;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Temp other = (Temp) obj;
    return index == other.index;
  }
  
  @Override
  public List<Expression> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitTemp(this);
  }

  @Override
  public SEXP getSExpression() {
    throw new UnsupportedOperationException();
  }
}
