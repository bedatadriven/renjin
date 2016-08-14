package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.repackaged.asm.Type;

public class PrimitiveFieldStrategy extends SingleFieldStrategy {
  
  private GimplePrimitiveType gimpleType;

  public PrimitiveFieldStrategy(Type ownerClass, String fieldName, GimplePrimitiveType fieldType) {
    super(ownerClass, fieldName, fieldType.jvmType());
    this.gimpleType = fieldType;
  }

  @Override
  public GExpr memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType) {
    
    JLValue fieldExpr = Expressions.field(instance, fieldType, fieldName);
    
    if(expectedType instanceof PrimitiveTypeStrategy) {
      PrimitiveTypeStrategy primitiveTypeStrategy = (PrimitiveTypeStrategy) expectedType;
      if(!fieldExpr.getType().equals(primitiveTypeStrategy.getJvmType())) {
        throw new UnsupportedOperationException("TODO: expectedType = " + expectedType);
      }

      if(size != 0 && (offset != 0 || size != gimpleType.getSize())) {
        if(!primitiveTypeStrategy.getJvmType().equals(Type.BYTE_TYPE)) {
          throw new UnsupportedOperationException(
              String.format("Unsupported bitfield: expected type = %s, offset = %d, size = %d", 
                  expectedType, offset, size));
        }
        fieldExpr = new BitFieldExpr(ownerClass, instance, fieldName, offset, size);
      }
      return new PrimitiveValue(fieldExpr);
      
    } else {
      throw new UnsupportedOperationException("expectedType: " + expectedType);
    }
  }
}
