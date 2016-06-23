package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.primitives.sequence.DoubleSequence;
import org.renjin.sexp.AtomicVector;

public class SequenceExpression extends SpecializedCallExpression {
 

  public SequenceExpression(Expression from, Expression to) {
    super(from, to);
  }

  @Override
  public boolean isFunctionDefinitelyPure() {
    return true;
  }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {
    int stackSizeIncrease =
        assertDouble(childAt(0)).emitPush(emitContext, mv) + 
        assertDouble(childAt(1)).emitPush(emitContext, mv);

    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DoubleSequence.class), "fromTo",
        Type.getMethodDescriptor(Type.getType(AtomicVector.class), Type.DOUBLE_TYPE, Type.DOUBLE_TYPE), false);

    return stackSizeIncrease;
  }

  private Expression assertDouble(Expression expression) {
    if(!expression.getType().equals(double.class)) {
      throw new AssertionError(expression + " has a type of " + expression.getType() + " expected double");
    }
    return expression;
  }

  @Override
  public Class getType() {
    return AtomicVector.class;
  }

  @Override
  public String toString() {
    return childAt(0) + ":" + childAt(1);
  }
}
