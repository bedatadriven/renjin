package r.compiler.ir.tac.operand;

import java.util.Collections;
import java.util.Set;

import r.lang.Context;

public class TempVariable implements Variable {
  private final int index;
  
  public TempVariable(int index) {
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
    return "\u03C4" + index;
  }

  @Override
  public Set<Variable> variables() {
    return Collections.<Variable>singleton(this);
  }

  @Override
  public Variable renameVariable(Variable name, Variable newName) {
    return this.equals(name) ? newName : this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + index;
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
    TempVariable other = (TempVariable) obj;
    return index == other.index;
  }
}
