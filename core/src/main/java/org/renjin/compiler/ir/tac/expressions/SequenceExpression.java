package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;

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
    throw new UnsupportedOperationException();
  }

  @Override
  public Class inferType() {
    throw new UnsupportedOperationException();
  }
}
