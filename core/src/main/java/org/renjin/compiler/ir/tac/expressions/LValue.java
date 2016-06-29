package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.VariableStorage;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.sexp.SEXP;

import java.util.Map;

/**
 * An {@code SimpleExpression} that can be the target of an assignment.
 */
public abstract class LValue implements SimpleExpression {

  private ValueBounds valueBounds = ValueBounds.UNBOUNDED;
  private Type type = Type.getType(SEXP.class);

  @Override
  public final int getChildCount() {
    return 0;
  }

  @Override
  public final Expression childAt(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public final void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public final int load(EmitContext emitContext, InstructionAdapter mv) {
    VariableStorage storage = emitContext.getVariableStorage(this);
    mv.load(storage.getSlotIndex(), storage.getType());
    return storage.getType().getSize();
  }

  @Override
  public final ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    ValueBounds type = typeMap.get(this);
    if(type == null) {
      valueBounds = ValueBounds.UNBOUNDED;
    } else {
      valueBounds = type;
    }
    this.type = valueBounds.storageType();
    return valueBounds;
  }

  @Override
  public final ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public Type getType() {
    return valueBounds.storageType();
  }
}

