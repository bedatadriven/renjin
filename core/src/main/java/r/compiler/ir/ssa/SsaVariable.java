package r.compiler.ir.ssa;

import java.util.Collections;
import java.util.Set;

import r.compiler.ir.IRUtils;
import r.compiler.ir.tac.expressions.SimpleExpression;
import r.compiler.ir.tac.expressions.Variable;
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
  public SimpleExpression replaceVariable(Variable name, Variable newName) {
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
}
