package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.primitives.sequence.DoubleSequence;
import org.renjin.sexp.AtomicVector;

import java.util.List;

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
  public void emitPush(EmitContext emitContext, MethodVisitor mv) {
    childAt(0).emitPush(emitContext, mv);
    childAt(1).emitPush(emitContext, mv);
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(DoubleSequence.class), "fromTo",
      "(DD)Lorg.renjin.primitives.sequence.DoubleSequence;" );
  }

  @Override
  public Class getType() {
    return AtomicVector.class;
  }

  @Override
  public boolean isTypeResolved() {
    return true;
  }
}
