package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Generates bytecode for calls to memset()
 */
public class MemSetGenerator implements CallGenerator {


  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    
//    ExprGenerator pointer = argumentGenerators.get(0);
//    ExprGenerator byteValue = argumentGenerators.get(1);
//    ExprGenerator length = argumentGenerators.get(2);
//
//    // memset signature is (array, offset, byteValue, length)
//
//    // push arguments on the stack
//    pointer.emitPushPtrArrayAndOffset(mv);
//    byteValue.load(mv);
//    length.load(mv);
//    invokeMemset(mv, pointer);
    throw new UnsupportedOperationException();
  }

//
//  private void invokeMemset(MethodGenerator mv, ExprGenerator pointer) {
//    // compose the signature based on the arguments
//    WrapperType wrapperType = WrapperType.forPointerType((GimpleIndirectType) pointer.getGimpleType());
//
//    mv.visitMethodInsn(Opcodes.INVOKESTATIC,
//        wrapperType.getWrapperType().getInternalName(),
//        "memset",
//        Type.getMethodDescriptor(Type.VOID_TYPE, wrapperType.getArrayType(), Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE),
//        false);
//  }

}
