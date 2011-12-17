package r.compiler.ir.tac.expressions;

import r.lang.Context;

/**
 * An {@code SimpleExpression} that can be the target of an assignment.
 */
public interface LValue extends SimpleExpression {
  
  void setValue(Context context, Object[] temp, Object value);


}

