package org.renjin.compiler.ir.tac.expressions;

import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
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

  public Expression getCounter() {
    return arguments[0];
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
    getCounter().emitPush(emitContext, mv);
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitInsn(Opcodes.IADD);
  }

  @Override
  public Class getType() {
    return int.class;
  }

  @Override
  public boolean isTypeResolved() {
    return true;
  }
}
