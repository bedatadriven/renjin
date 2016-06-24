package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.TypeBounds;

import java.util.Map;


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
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {
    getCounter().emitPush(emitContext, mv);
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitInsn(Opcodes.IADD);
    return 2;
  }

  @Override
  public TypeBounds computeTypeBounds(Map<LValue, TypeBounds> typeMap) {
    return TypeBounds.INT_PRIMITIVE;
  }
}
