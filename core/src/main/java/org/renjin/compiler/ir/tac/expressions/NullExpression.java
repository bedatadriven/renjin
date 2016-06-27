package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.ValueBounds;

import java.util.Map;


/**
 * A nullary expression (used by the Goto statement, for example,
 * which as no rhs expression. (Not the same as a constant Null.INSTANCE !)
 */

public class NullExpression implements Expression {

  public static final NullExpression INSTANCE = new NullExpression();
  
  private NullExpression() { }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Type getType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ValueBounds getValueBounds() {
    return ValueBounds.UNBOUNDED;
  }

  @Override
  public void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException();
  }
}
