package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrValueFunction;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

/**
 * A field that is the union of more than one pointer types.
 * We store the value as a single Object instance.
 */
public class PointerUnionField extends FieldStrategy {

  private static final Type OBJECT_TYPE = Type.getType(Object.class);
  
  private Type declaringClass;
  private String fieldName;

  public PointerUnionField(Type declaringClass, String fieldName) {
    this.declaringClass = declaringClass;
    this.fieldName = fieldName;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, fieldName, OBJECT_TYPE.getDescriptor(), null, null);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int fieldOffset, GimpleType expectedType) {
    JLValue fieldExpr = Expressions.field(instance, Type.getType(Object.class), fieldName);

    if(expectedType == null) {
      return new VoidPtr(fieldExpr);
    }

    if(expectedType.isPointerTo(GimplePrimitiveType.class)) {
      GimplePrimitiveType baseType = expectedType.getBaseType();
      return new PrimitiveFatPtrExpr(fieldExpr, baseType);
      
    } else {
      throw new UnsupportedOperationException("TODO: " + expectedType);
    }
  }
  
  private class PrimitiveFatPtrExpr implements FatPtr {

    private JLValue fieldExpr;
    private GimplePrimitiveType baseType;

    public PrimitiveFatPtrExpr(JLValue fieldExpr, GimplePrimitiveType baseType) {
      this.fieldExpr = fieldExpr;
      this.baseType = baseType;
    }

    @Override
    public Type getValueType() {
      return baseType.jvmType();
    }

    @Override
    public boolean isAddressable() {
      return false;
    }

    @Override
    public JExpr wrap() {
      return fieldExpr;
    }

    @Override
    public FatPtrPair toPair(MethodGenerator mv) {
      return toPair();
    }

    @Override
    public FatPtrPair toPair() {
      Type wrapperType = Wrappers.wrapperType(baseType.jvmType());
      JExpr wrapper = Expressions.cast(fieldExpr, wrapperType);
      
      return Wrappers.toPair(new VoidPtrValueFunction(), wrapper);
      
    }

    @Override
    public void store(MethodGenerator mv, GExpr rhs) {
      if(rhs instanceof FatPtr) {
        fieldExpr.store(mv, ((FatPtr) rhs).wrap());
      } else {
        throw new UnsupportedOperationException("TODO: " + rhs.getClass().getName());
      }
    }

    @Override
    public GExpr addressOf() {
      throw new NotAddressableException();
    }
  }
  
}
