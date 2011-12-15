package r.compiler.ir.ssa;

import java.util.Collections;
import java.util.Set;

import r.compiler.ir.tac.operand.SimpleExpr;
import r.compiler.ir.tac.operand.Variable;
import r.lang.Context;

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
  public SimpleExpr renameVariable(Variable name, Variable newName) {
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
    
    String subscript = Integer.toString(version+1);
    for(int i=0; i!=subscript.length(); ++i) {
      int digit = subscript.charAt(i) - '0';
      sb.appendCodePoint(0x2080 + digit);
    }
    
    return sb.toString();
  }
}
