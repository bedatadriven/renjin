package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.TypeBounds;

import java.util.Map;


/**
 * The length of an expression.
 * 
 * <p>This is a bit annoying to add this to the set of expressions,
 * but we need it to translate for expressions, because the length
 * primitive is generic, but the for loop always uses the actual length of the
 * vector. (is this another sign we need to push 'fors' down into the IR level?)
 */
public class Length extends SpecializedCallExpression implements SimpleExpression {

  public Length(Expression vector) {
    super(vector);
  }

  public Expression getVector() {
    return arguments[0];
  }

  @Override
  public boolean isFunctionDefinitelyPure() {
    return true;
  }

  @Override
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {
    int stackSizeIncrease = getVector().emitPush(emitContext, mv);
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/renjin/sexp/SEXP", "length", "()I");
    return stackSizeIncrease;
  }

  @Override
  public String toString() {
    return "length(" + getVector() + ")";
  }

  @Override
  public TypeBounds computeTypeBounds(Map<LValue, TypeBounds> typeMap) {
    return TypeBounds.LOGICAL_PRIMITIVE;
  }

}
