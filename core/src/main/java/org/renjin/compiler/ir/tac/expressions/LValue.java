package org.renjin.compiler.ir.tac.expressions;

import com.google.common.base.Preconditions;
import java_cup.emit;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.ssa.VariableMap;
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
  public final int emitPush(EmitContext emitContext, MethodVisitor mv) {
    Class type = getType();

    int register = emitContext.getRegister(this);

    if(type.equals(double.class)) {
      mv.visitVarInsn(Opcodes.DLOAD, register);
      return 2;

    } else if(type.equals(int.class)) {
      mv.visitVarInsn(Opcodes.ILOAD, register);
      return 1;

    } else if(Vector.class.isAssignableFrom(type)) {
      mv.visitVarInsn(Opcodes.ALOAD, register);
      return 1;

    } else {
      throw new UnsupportedOperationException(this + ":" + type);
    }
  }

  public Class getType() {
    if(type == null) {
      throw new IllegalStateException("Type of " + this + "  " +
          System.identityHashCode(this) +
          " (" + this.getClass().getSimpleName() + ") is not resolved");
    }
    return type;
  }

  @Override
  public Class resolveType(VariableMap variableMap) {
    if(type != null) {
      return type;
    }

    Expression rvalue = variableMap.getDefinition(this);
    if(rvalue == null) {
      throw new UnsupportedOperationException(this + " is not defined");
    }
    try {
      type = rvalue.resolveType(variableMap);
      System.out.println("Resolved " + this + "@" + System.identityHashCode(this) + " => " + type);
      return (type =  rvalue.resolveType(variableMap));
    } catch(Exception e) {
      throw new UnsupportedOperationException("Cannot resolve type of " + this, e);
    }
  }
}

