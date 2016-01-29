package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ReturnStrategy;

import java.util.List;

/**
 * Strategy for returning from a void-typed function.
 * 
 * <p>Only {@link VoidReturnStrategy#emitReturnDefault(MethodGenerator)} is supported, 
 * {@link VoidReturnStrategy#emitReturnValue(MethodGenerator, ExprGenerator)} always
 * throws {@code UnsupportedOperationException}</p>
 */
public class VoidReturnStrategy implements ReturnStrategy {

  @Override
  public Type getType() {
    return Type.VOID_TYPE;
  }

  @Override
  public void emitReturnValue(MethodGenerator mv, ExprGenerator valueGenerator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void emitReturnDefault(MethodGenerator mv) {
    mv.visitInsn(Opcodes.RETURN);
  }

  @Override
  public ExprGenerator callExpression(CallGenerator callGenerator, List<ExprGenerator> arguments) {
    throw new UnsupportedOperationException("Void is not an expression");
  }
}
