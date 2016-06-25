package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.sexp.*;

import java.util.Map;


/**
 * A value known at compile time.
 */
public final class Constant implements SimpleExpression {

  public static final Constant NULL = new Constant(Null.INSTANCE);
  public static final Constant TRUE = new Constant(Logical.TRUE);
  public static final Constant FALSE = new Constant(Logical.FALSE);
  public static final Constant NA = new Constant(Logical.NA);

  private SEXP value;
  private ValueBounds valueBounds;

  public Constant(SEXP value) {
    this.value = value;
    this.valueBounds = ValueBounds.of(value);
  }
  
  public Constant(int value) {
    this(IntVector.valueOf(value));
  }

  public Constant(Logical value) {
    this(LogicalVector.valueOf(value));
  }

  public SEXP getValue() {
    return value;
  }

  @Override
  public final int getChildCount() {
    return 0;
  }

  @Override
  public final Expression childAt(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public final boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public final void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public ValueBounds computeTypeBounds(Map<Expression, ValueBounds> variableMap) {
    return valueBounds;
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
