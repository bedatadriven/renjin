package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
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
  private Type type;

  public Constant(SEXP value) {
    this.value = value;
    this.valueBounds = ValueBounds.of(value);
    this.type = valueBounds.storageType();
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
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    if (type.equals(Type.INT_TYPE)) {
      mv.iconst(((AtomicVector) value).getElementAsInt(0));

    } else if (type.equals(Type.DOUBLE_TYPE)) {
      mv.dconst(((AtomicVector) value).getElementAsDouble(0));

    } else if (type.equals(Type.getType(String.class))) {
      mv.aconst(((AtomicVector) value).getElementAsString(0));

    } else {
      throw new UnsupportedOperationException("type: " + type);
    }
    return 1;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public final void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    return valueBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
