package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates fields that are pointers to pointers
 */
public class PrimitivePtrPtrFieldGenerator implements FieldGenerator {
  
  private String className;
  private String fieldName;
  private GimpleType pointerType;
  private GimplePrimitiveType primitiveType;
  private WrapperType wrapperType;
  
  public PrimitivePtrPtrFieldGenerator(String className, String fieldName, GimpleType type) {
    this.className = className;
    this.fieldName = fieldName;
    this.pointerType = type;
    this.primitiveType = type.getBaseType().getBaseType();
    this.wrapperType = WrapperType.of(primitiveType);
  }

  @Override
  public void emitStaticField(ClassVisitor cv) {
    emitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, cv);
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(Opcodes.ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, wrapperType.getArrayType().getDescriptor(), null, null).visitEnd();
    cv.visitField(access, fieldName + "$offset", "I", null, null).visitEnd();
  }

  @Override
  public ExprGenerator staticExprGenerator() {
    return new StaticFieldPtrPtr();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    throw new UnsupportedOperationException("todo");
  }
  
  private class StaticFieldPtrPtr extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return pointerType;
    }
    
    
  }
  
}
