package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.GimpleVoidType;


public class VoidPtrField extends FieldGenerator {

  private final String className;
  private final String arrayFieldName;
  private final String offsetFieldName;
  private final String arrayFieldDescriptor;

  public VoidPtrField(String className, String fieldName) {
    this.className = className;
    this.arrayFieldName = fieldName;
    this.offsetFieldName = fieldName + "$offset";
    this.arrayFieldDescriptor = "[Ljava/lang/Object;";
  }

  @Override
  public GimpleType getType() {
    return new GimplePointerType(new GimpleVoidType());
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, arrayFieldName, arrayFieldDescriptor, null, null).visitEnd();
    cv.visitField(Opcodes.ACC_PUBLIC, offsetFieldName, "I", null, 0).visitEnd();
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
      return VoidPtrField.this.getType();
    }

    @Override
    public void emitPushArray(MethodVisitor mv) {
      instanceGenerator.emitPushRecordRef(mv);
      mv.visitFieldInsn(Opcodes.GETFIELD, className, arrayFieldName, arrayFieldDescriptor);
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      instanceGenerator.emitPushRecordRef(mv);
      mv.visitInsn(Opcodes.DUP);
      // stack: (instance, instance)
      mv.visitFieldInsn(Opcodes.GETFIELD, className, arrayFieldName, arrayFieldDescriptor);
      // stack: (instance, array)
      mv.visitInsn(Opcodes.SWAP);
      // stack: (array, instance)
      mv.visitFieldInsn(Opcodes.GETFIELD, className, offsetFieldName, "I");
      // stack: (array, offset)
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator ptr) {

      instanceGenerator.emitPushRecordRef(mv);
      
      // Need two copies of the instance on the stack for the store stores
      mv.visitInsn(Opcodes.DUP);

      // Push array and offset onto the stack
      ptr.emitPushPtrArrayAndOffset(mv);

      // DUP_X2 + POP
      //             value3,  value2, value1 →            value1,   value3,  value2,  value1
      // instance, instance,   array, offset → instance,  offset, instance ,  array,  offset
      //                                     → instance,  offset, instance ,  array            
      mv.visitInsn(Opcodes.DUP_X2);
      mv.visitInsn(Opcodes.POP);

      // Consume [instance, array] -> 
      mv.visitFieldInsn(Opcodes.PUTFIELD, className, arrayFieldName, arrayFieldDescriptor);

      // Consume [instance, offset] 
      mv.visitFieldInsn(Opcodes.PUTFIELD, className, offsetFieldName, "I");
    }
  }
}
