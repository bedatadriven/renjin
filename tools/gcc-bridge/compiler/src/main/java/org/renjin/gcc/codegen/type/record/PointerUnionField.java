package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;


public class PointerUnionField extends FieldStrategy {
  
  private Type declaringClass;
  private String fieldName;

  public PointerUnionField(Type declaringClass, String fieldName) {
    this.declaringClass = declaringClass;
    this.fieldName = fieldName;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, fieldName, Type.getDescriptor(Object.class), null, null);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int fieldOffset, GimpleType expectedType) {
    JExpr voidPtr = Expressions.field(instance, Type.getType(Object.class), fieldName);

    if(expectedType == null) {
      return new VoidPtr(voidPtr);
    }

    if(expectedType.isPointerTo(GimplePrimitiveType.class)) {
      GimplePrimitiveType baseType = expectedType.getBaseType();
      JExpr wrapperInstance = Expressions.cast(voidPtr, Wrappers.wrapperType(baseType.jvmType()));
      JExpr unwrappedArray = Wrappers.arrayField(wrapperInstance, baseType.jvmType());
      JExpr unwrappedOffset = Wrappers.offsetField(wrapperInstance);

      return new FatPtrExpr(unwrappedArray, unwrappedOffset);
    } else {
      throw new UnsupportedOperationException("TODO: " + expectedType);
    }
  }
}
