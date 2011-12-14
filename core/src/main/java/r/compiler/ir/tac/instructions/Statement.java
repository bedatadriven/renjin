package r.compiler.ir.tac.instructions;

import java.util.Set;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.operand.Variable;
import r.lang.Context;

public interface Statement {
 
  Object interpret(Context context, Object temp[]);
  
  Iterable<IRLabel> possibleTargets();
  
  Set<Variable> variables();
}