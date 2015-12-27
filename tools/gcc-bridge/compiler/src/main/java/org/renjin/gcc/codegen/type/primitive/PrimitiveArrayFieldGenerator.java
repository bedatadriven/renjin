package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.PUTFIELD;


public class PrimitiveArrayFieldGenerator extends FieldGenerator {
  private String className;
  private String fieldName;
  private GimpleArrayType arrayType;
  private GimplePrimitiveType componentType;
  private final String fieldDescriptor;
  
  public PrimitiveArrayFieldGenerator(String className, String fieldName, GimpleArrayType arrayType) {
    this.className = className;
    this.fieldName = fieldName;
    this.arrayType = arrayType;
    this.componentType = (GimplePrimitiveType) arrayType.getComponentType();
    this.fieldDescriptor = "[" + componentType.jvmType().getDescriptor();
  }

  @Override
  public GimpleType getType() {
    return arrayType;
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(ACC_PUBLIC, fieldName, fieldDescriptor, null, null).visitEnd();
  }

  @Override
  public void emitInstanceInit(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, 0); // this
    PrimitiveConstGenerator.emitInt(mv, arrayType.getElementCount());
    MallocGenerator.emitNewArray(mv, componentType.jvmType());
    mv.visitFieldInsn(Opcodes.PUTFIELD, className, fieldName, fieldDescriptor);
  }

  @Override
  public void emitStoreMember(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushArray(mv);
    mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldDescriptor);
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new MemberExpr(instanceGenerator);
  }
  
  private class MemberExpr extends AbstractExprGenerator {

    private ExprGenerator instanceGenerator;

    public MemberExpr(ExprGenerator instanceGenerator) {
      this.instanceGenerator = instanceGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return arrayType;
    }

    @Override
    public void emitPushArray(MethodVisitor mv) {
      instanceGenerator.emitPushRecordRef(mv);
      mv.visitFieldInsn(Opcodes.GETFIELD, className, fieldName, fieldDescriptor); 
    }

    @Override
    public ExprGenerator elementAt(ExprGenerator indexGenerator) {
      return new PrimitiveArrayElement(this, indexGenerator);
    }

    @Override
    public ExprGenerator addressOf() {
      return new AddressOfPrimitiveArray(this);
    }
  }

}
