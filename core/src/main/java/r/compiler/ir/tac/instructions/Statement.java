package r.compiler.ir.tac.instructions;

import r.compiler.ir.tac.IRLabel;
import r.lang.Context;

public interface Statement {
 
  Object interpret(Context context, Object temp[]);
  
  Iterable<IRLabel> possibleTargets();
}