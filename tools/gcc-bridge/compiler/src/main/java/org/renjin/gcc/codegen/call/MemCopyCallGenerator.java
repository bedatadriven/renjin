package org.renjin.gcc.codegen.call;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.statement.GimpleCall;


public class MemCopyCallGenerator implements CallGenerator {
  
  public static final String NAME = "__builtin_memcpy";


  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    
    if(call.getOperands().size() != 3) {
      throw new InternalCompilerException("__builtin_memcpy expects 3 args.");
    }
    ExprGenerator destination = exprFactory.findPointerGenerator(call.getOperand(0));
    ExprGenerator source =  exprFactory.findPointerGenerator(call.getOperand(1));
    ExprGenerator length = exprFactory.findValueGenerator(call.getOperand(2));
    
    throw new UnsupportedOperationException("TODO");
//
//    source.emitPushPtrArrayAndOffset(mv);
//    destination.emitPushPtrArrayAndOffset(mv);
//    length.load(mv);
//
//    // public static native void arraycopy(
//    //     Object src,  int  srcPos,
//    // Object dest, int destPos,
//    // int length);
//    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(System.class), "arraycopy", 
//        Type.getMethodDescriptor(Type.VOID_TYPE, 
//              Type.getType(Object.class), Type.INT_TYPE, 
//              Type.getType(Object.class), Type.INT_TYPE,
//              Type.INT_TYPE), false);
//
//    throw new UnsupportedOperationException();
  }

}
