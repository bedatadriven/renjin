package r.compiler.ir.tac.instructions;

import r.compiler.ir.tac.Node;
import r.lang.Context;

public interface Statement extends Node{
  Object interpret(Context context, Object temp[]);
}