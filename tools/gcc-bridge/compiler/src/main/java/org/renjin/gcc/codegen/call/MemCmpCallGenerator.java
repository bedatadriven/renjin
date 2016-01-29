package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveReturnStrategy;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.LongPtr;

import java.util.List;


public class MemCmpCallGenerator implements CallGenerator {
  
  @Override
  public void emitCall(MethodGenerator mv, List<ExprGenerator> argumentGenerators) {
    ExprGenerator p1 = argumentGenerators.get(0);
    ExprGenerator p2 = argumentGenerators.get(1);
    ExprGenerator n = argumentGenerators.get(2);
    
    GimpleType baseType = p1.getGimpleType().getBaseType();

    if(baseType instanceof GimplePrimitiveType) {
      p1.emitPushPtrArrayAndOffset(mv);
      p2.emitPushPtrArrayAndOffset(mv);
      n.emitPrimitiveValue(mv);

      Type type = ((GimplePrimitiveType) baseType).jvmType();
      
      if(type.equals(Type.DOUBLE_TYPE)) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DoublePtr.class), "memcmp", "([DI[DII)I", false);

      } else if(type.equals(Type.LONG_TYPE)) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(LongPtr.class), "memcmp", "([JI[JII)I", false);
        
      } else {
        throw new UnsupportedOperationException("Todo: " + type); 
      }
    } else {
      throw new UnsupportedOperationException("Unsupported type: " + baseType);
    }
  }

  @Override
  public void emitCallAndPopResult(MethodGenerator visitor, List<ExprGenerator> argumentGenerators) {
    // this function has no side effects
    
  }

  @Override
  public ExprGenerator expressionGenerator(GimpleType returnType, List<ExprGenerator> argumentGenerators) {
    return new PrimitiveReturnStrategy(new GimpleIntegerType(32))
        .callExpression(this, argumentGenerators);
  }

}
