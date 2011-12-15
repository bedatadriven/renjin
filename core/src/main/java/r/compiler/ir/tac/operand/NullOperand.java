package r.compiler.ir.tac.operand;

import java.util.Collections;
import java.util.Set;

import r.lang.Context;

public class NullOperand implements Operand {

  public static final NullOperand INSTANCE = new NullOperand();
  
  private NullOperand() { }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }

  @Override
  public Operand renameVariable(Variable name, Variable newName) {
    return this;
  }
  
}
