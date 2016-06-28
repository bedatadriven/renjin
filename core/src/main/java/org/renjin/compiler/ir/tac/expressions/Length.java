package org.renjin.compiler.ir.tac.expressions;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;

import java.util.Map;


/**
 * The length of an expression.
 * 
 * <p>This is a bit annoying to add this to the set of expressions,
 * but we need it to translate for expressions, because the length
 * primitive is generic, but the for loop always uses the actual length of the
 * vector.
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
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    int stackSizeIncrease = getVector().load(emitContext, mv);
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/renjin/sexp/SEXP", "length", "()I", true);
    return stackSizeIncrease;
  }

  @Override
  public Type getType() {
    return Type.INT_TYPE;
  }

  @Override
  public String toString() {
    return "length(" + getVector() + ")";
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    return ValueBounds.INT_PRIMITIVE;
  }

  @Override
  public ValueBounds getValueBounds() {
    return ValueBounds.INT_PRIMITIVE;
  }

}
