package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.gcc.codegen.type.ValueReturnStrategy;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.LongPtr;

import java.util.List;


public class MemCmpCallGenerator implements CallGenerator {

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    
//    
//    PtrExpr p1 = exprFactory.findPointerGenerator(call.getOperand(0));
//    PtrExpr p2 = exprFactory.findPointerGenerator(call.getOperand(1));
//    ExprGenerator n = exprFactory.findValueGenerator(call.getOperand(2));
//    
//    
    
//    GimpleType baseType = p1.getGimpleType().getBaseType();
//
//    if(baseType instanceof GimplePrimitiveType) {
//      p1.emitPushPtrArrayAndOffset(mv);
//      p2.emitPushPtrArrayAndOffset(mv);
//      n.load(mv);
//
//      Type type = ((GimplePrimitiveType) baseType).jvmType();
//      
//      if(type.equals(Type.DOUBLE_TYPE)) {
//        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DoublePtr.class), "memcmp", "([DI[DII)I", false);
//
//      } else if(type.equals(Type.LONG_TYPE)) {
//        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(LongPtr.class), "memcmp", "([JI[JII)I", false);
//        
//      } else {
//        throw new UnsupportedOperationException("Todo: " + type); 
//      }
//    } else {
//      throw new UnsupportedOperationException("Unsupported type: " + baseType);
//    }
    throw new UnsupportedOperationException();
  }
}
