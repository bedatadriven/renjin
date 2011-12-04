package r.compiler.ir.tac.instructions;

import r.lang.Context;

public interface Statement {
  Object interpret(Context context, Object temp[]);
}