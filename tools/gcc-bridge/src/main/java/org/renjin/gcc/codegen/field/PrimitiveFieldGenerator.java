package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.ValueGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;


public class PrimitiveFieldGenerator extends AbstractExprGenerator implements FieldGenerator, ValueGenerator {

  private String fieldName;
  private String className;
  private GimplePrimitiveType gimpleType;
  private Type type;

  public PrimitiveFieldGenerator(String className, String fieldName, GimpleType gimpleType, Type type) {
    this.fieldName = fieldName;
    this.className = className;
    this.gimpleType = (GimplePrimitiveType) gimpleType;
    this.type = type;
  }


  @Override
  public void emitField(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, fieldName, type.getDescriptor(), null, null).visitEnd();
  }
  
  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    mv.visitFieldInsn(Opcodes.GETSTATIC, className, fieldName, type.getDescriptor());
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, type.getDescriptor());
  }

  @Override
  public GimpleType getGimpleType() {
    return gimpleType;
  }
}
