package r.compiler.ir.tac.operand;

import r.lang.Context;

public interface LValue extends Operand {

  void setValue(Context context, Object[] temp, Object value);
  
}
