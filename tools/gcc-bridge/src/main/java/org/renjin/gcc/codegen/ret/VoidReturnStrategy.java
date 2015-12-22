package org.renjin.gcc.codegen.ret;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;

import java.util.List;

/**
 * Strategy for returning from a void-typed function.
 * 
 * <p>Only {@link VoidReturnStrategy#emitReturnDefault(MethodVisitor)} is supported, 
 * {@link VoidReturnStrategy#emitReturnValue(MethodVisitor, ExprGenerator)} always
 * throws {@code UnsupportedOperationException}</p>
 */
public class VoidReturnStrategy implements ReturnStrategy {

  @Override
  public Type getType() {
    return Type.VOID_TYPE;
  }

  @Override
  public void emitReturnValue(MethodVisitor mv, ExprGenerator valueGenerator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void emitReturnDefault(MethodVisitor mv) {
    mv.visitInsn(Opcodes.RETURN);
  }

  @Override
  public ExprGenerator callExpression(CallGenerator callGenerator, List<ExprGenerator> arguments) {
    throw new UnsupportedOperationException("Void is not an expression");
  }
}
