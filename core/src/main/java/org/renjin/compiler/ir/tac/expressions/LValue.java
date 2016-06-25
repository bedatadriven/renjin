package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.ValueBounds;

import java.util.Map;

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
  public final int emitPush(EmitContext emitContext, MethodVisitor mv) {
//    Class type = getType();
//
//    int register = emitContext.getRegister(this);
//
//    if(type.equals(double.class)) {
//      mv.visitVarInsn(Opcodes.DLOAD, register);
//      return 2;
//
//    } else if(type.equals(int.class)) {
//      mv.visitVarInsn(Opcodes.ILOAD, register);
//      return 1;
//
//    } else if(Vector.class.isAssignableFrom(type)) {
//      mv.visitVarInsn(Opcodes.ALOAD, register);
//      return 1;
//
//    } else {
//      throw new UnsupportedOperationException(this + ":" + type);
//    }
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ValueBounds computeTypeBounds(Map<Expression, ValueBounds> variableMap) {
    ValueBounds type = variableMap.get(this);
    if(type == null) {
      return ValueBounds.UNBOUNDED;
    }
    return type;
  }
}

