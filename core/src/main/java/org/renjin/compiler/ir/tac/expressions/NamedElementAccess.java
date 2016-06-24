package org.renjin.compiler.ir.tac.expressions;


import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;

/**
 * Element access in the form x$name
 */
public class NamedElementAccess extends SpecializedCallExpression {

  private String memberName;


  public NamedElementAccess(Expression expression, String memberName) {
    super(expression);
    this.memberName = memberName;
  }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {
    throw new UnsupportedOperationException();
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
