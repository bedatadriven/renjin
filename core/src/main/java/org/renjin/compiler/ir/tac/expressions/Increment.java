package org.renjin.compiler.ir.tac.expressions;

import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;


/**
 * Increments a counter variable. Only used for the 
 * 'for' loop, will see if really need this
 * 
 */
public class Increment extends SpecializedCallExpression {

    public Increment(LValue counter) {
    super(counter);
  }

  @Override
  public String toString() {
    return "increment counter " + arguments[0];
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
    return int.class;
  }
}
