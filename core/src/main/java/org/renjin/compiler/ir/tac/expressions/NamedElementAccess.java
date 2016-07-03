package org.renjin.compiler.ir.tac.expressions;


import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

/**
 * Element access in the form x$name
 */
public class NamedElementAccess extends SpecializedCallExpression {

  private String memberName;
  private ValueBounds valueBounds = ValueBounds.UNBOUNDED;

  public NamedElementAccess(Expression expression, String memberName) {
    super(expression);
    this.memberName = memberName;
  }

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
    return valueBounds.storageType();
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public boolean isFunctionDefinitelyPure() {
    return true;
  }

  @Override
  public String toString() {
    return arguments[0] + "$" + memberName;
  }
}
