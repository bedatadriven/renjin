package org.renjin.compiler.ir.tac.expressions;

import org.renjin.eval.Context;

/**
 * An {@code SimpleExpression} that can be the target of an assignment.
 */
public interface LValue extends SimpleExpression {
  
  void setValue(Context context, Object[] temp, Object value);

  LValue replaceVariable(Variable name, Variable newName);


}

