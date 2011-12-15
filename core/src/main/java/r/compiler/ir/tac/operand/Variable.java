package r.compiler.ir.tac.operand;

import r.lang.Context;

public interface Variable extends SimpleExpr {

  void setValue(Context context, Object[] temp, Object value);
  
}
