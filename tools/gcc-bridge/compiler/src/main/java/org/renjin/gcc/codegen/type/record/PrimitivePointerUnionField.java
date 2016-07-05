package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

/**
 * A field which can point to any primitive type.
 */
public class PrimitivePointerUnionField extends FieldStrategy {

  private Type declaringClass;
  private String name;

  public PrimitivePointerUnionField(Type declaringClass, String name) {
    this.declaringClass = declaringClass;
    this.name = name;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, name, Type.getDescriptor(Object.class), null, null);
    cv.visitField(Opcodes.ACC_PUBLIC, name + "$offset", Type.INT_TYPE.getDescriptor(), null, null);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int fieldOffset, GimpleType expectedType) {
    JLValue arrayExpr = Expressions.field(instance, Type.getType(Object.class), name);
    JLValue offsetExpr = Expressions.field(instance, Type.INT_TYPE, name + "$offset");
    
    if(expectedType == null) {
      return new FatPtrExpr(arrayExpr, offsetExpr);

    } else if(expectedType.isPointerTo(GimplePrimitiveType.class)) {
      GimplePrimitiveType baseType = expectedType.getBaseType();
      Type expectedArrayType = Wrappers.valueArrayType(baseType.jvmType());
      JExpr castedArrayExpr = Expressions.cast(arrayExpr, expectedArrayType);
      
      return new FatPtrExpr(castedArrayExpr, offsetExpr);
    
    } else {
      throw new UnsupportedOperationException("Type: " + expectedType);
    }
  }
}
