package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.ir.ValueBounds;

public interface ValueBoundsMap {

  ValueBounds get(Expression expression);
}
