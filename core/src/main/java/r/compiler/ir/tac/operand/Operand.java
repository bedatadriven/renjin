package r.compiler.ir.tac.operand;

import java.util.Set;

import r.lang.Context;

public interface Operand  {

  Object retrieveValue(Context context, Object temps[]);
  
  Set<Variable> variables();

  Operand renameVariable(Variable name, Variable newName);
  
}
