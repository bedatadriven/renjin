package r.compiler.ir.tac.operand;

import r.lang.Context;

public interface Operand  {

  Object retrieveValue(Context context, Object temps[]);
  
}
