package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.pointers.DereferencedPrimitiveValue;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;

/**
 * FieldGenerator for primitive fields that must be addressed. 
 */
public class AddressablePrimitiveField extends FieldGenerator {

  private String fieldName;
  private String className;
  private GimplePrimitiveType gimpleType;
  private Type type;
  private String fieldDescriptor;

  public AddressablePrimitiveField(String className, String fieldName, GimpleType gimpleType, Type type) {
    this.fieldName = fieldName;
    this.className = className;
    this.gimpleType = (GimplePrimitiveType) gimpleType;
    this.type = type;
    this.fieldDescriptor = "[" + type.getDescriptor();
  }


  @Override
  public GimpleType getType() {
    return gimpleType;
  }

  @Override
  public void emitInstanceInit(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    mv.visitInsn(ICONST_1);
    MallocGenerator.emitNewArray(mv, type);
    mv.visitFieldInsn(Opcodes.PUTFIELD, className, fieldName, fieldDescriptor);
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(ACC_PUBLIC, fieldName, fieldDescriptor, null, null).visitEnd();
  }

  @Override
  public void emitStoreMember(MethodVisitor mv, ExprGenerator valueGenerator) {
    mv.visitFieldInsn(GETFIELD, className, fieldName, fieldDescriptor);
    mv.visitInsn(Opcodes.ICONST_0);
    valueGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(type.getOpcode(IASTORE));
  }


  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new DereferencedPrimitiveValue(new MemberAddressOf(instanceGenerator));
  }

  private class MemberAddressOf extends AbstractExprGenerator {
    private ExprGenerator instanceGenerator;

    public MemberAddressOf(ExprGenerator instanceGenerator) {
      this.instanceGenerator = instanceGenerator;
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      instanceGenerator.emitPushRecordRef(mv);
      mv.visitFieldInsn(Opcodes.GETFIELD, className, fieldName, fieldDescriptor);
      mv.visitInsn(Opcodes.ICONST_0);
    }

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(gimpleType);
    }
  }
}
