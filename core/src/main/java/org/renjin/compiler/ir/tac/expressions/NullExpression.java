package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;

import java.util.Collections;
import java.util.List;


/**
 * A nullary expression (used by the Goto statement, for example,
 * which as no rhs expression. (Not the same as a constant Null.INSTANCE !)
 */

public class NullExpression implements Expression {

  public static final NullExpression INSTANCE = new NullExpression();
  
  private NullExpression() { }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public void emitPush(EmitContext emitContext, MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class getType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void resolveType() {

  }

  @Override
  public boolean isTypeResolved() {
    return true;
  }

  @Override
  public void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException();
  }
}
