package org.renjin.compiler.ir.tac.expressions;

import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.eval.Context;
import org.renjin.sexp.Vector;

/**
 * An {@code SimpleExpression} that can be the target of an assignment.
 */
public abstract class LValue implements SimpleExpression {

  private Class type;


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
  public Class getType() {
    if(type == null) {
      throw new IllegalStateException("type has not been set yet: " + this);
    }
    return type;
  }

  public void setType(Class type) {
    this.type = type;
  }

  @Override
  public boolean isTypeResolved() {
    return type != null;
  }

  @Override
  public final void emitPush(EmitContext emitContext, MethodVisitor mv) {
    Preconditions.checkNotNull(type, "type not resolved for " + this);

    int register = emitContext.getRegister(this);

    if(type.equals(double.class)) {
      mv.visitVarInsn(Opcodes.DLOAD, register);
    } else if(type.equals(int.class)) {
      mv.visitVarInsn(Opcodes.ILOAD, register);
    } else if(Vector.class.isAssignableFrom(type)) {
      mv.visitVarInsn(Opcodes.ALOAD, register);
    } else {
      throw new UnsupportedOperationException(this + ":" + type);
    }

  }

  @Override
  public void resolveType() {
    throw new UnsupportedOperationException("resolveType() is not supported by LValue expressions");
  }
}

