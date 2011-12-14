package r.compiler.ir.tac.operand;

import java.util.Collections;
import java.util.Set;

import r.lang.Context;

public class Temp implements LValue, SimpleExpr {
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
    return "_t" + index;
  }

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }
}
