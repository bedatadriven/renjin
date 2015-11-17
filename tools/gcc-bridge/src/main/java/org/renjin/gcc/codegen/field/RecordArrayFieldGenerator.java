package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class RecordArrayFieldGenerator extends FieldGenerator {

  private GimpleArrayType arrayType;
  private String className;
  private String fieldName;
  private RecordClassGenerator recordGenerator;
  private String fieldDescriptor;

  public RecordArrayFieldGenerator(String className, String fieldName, 
                                   RecordClassGenerator recordGenerator, GimpleArrayType arrayType) {
    this.className = className;
    this.fieldName = fieldName;
    this.recordGenerator = recordGenerator;
    this.arrayType = arrayType;
    fieldDescriptor = "[" + recordGenerator.getDescriptor();
  }

  @Override
  public GimpleType getType() {
    return arrayType;
  }

  @Override
  public void emitStaticField(ClassVisitor cv, GimpleVarDecl decl) {
    emitField(ACC_PUBLIC | ACC_STATIC, cv);
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, fieldDescriptor, null, null).visitEnd();
  }

  @Override
  public ExprGenerator staticExprGenerator() {
    return new StaticArrayValue();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    throw new UnsupportedOperationException();
  }

  private class StaticArrayValue extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return arrayType;
    }

    @Override
    public ExprGenerator addressOf() {
      return new StaticArrayPtr();
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      valueGenerator.emitPushArray(mv);
      mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, fieldDescriptor);
    }

    @Override
    public void emitPushArray(MethodVisitor mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, fieldName, fieldDescriptor);
    }

    @Override
    public ExprGenerator elementAt(ExprGenerator indexGenerator) {
      return new StaticArrayElement(indexGenerator);
    }
  }
  
  private class StaticArrayPtr extends AbstractExprGenerator {
    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(arrayType);
    }

    @Override
    public WrapperType getPointerType() {
      return WrapperType.OBJECT_PTR;
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, fieldName, fieldDescriptor);
      mv.visitInsn(Opcodes.ICONST_0);
    }
  }
  
  private class StaticArrayElement extends AbstractExprGenerator {
    
    private ExprGenerator indexGenerator;

    public StaticArrayElement(ExprGenerator indexGenerator) {
      this.indexGenerator = indexGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return recordGenerator.getGimpleType();
    }

    @Override
    public void emitPushRecordRef(MethodVisitor mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, fieldName, fieldDescriptor);
      indexGenerator.emitPrimitiveValue(mv);
      mv.visitInsn(Opcodes.AALOAD);
    }

    @Override
    public ExprGenerator memberOf(String memberName) {
      return recordGenerator.getFieldGenerator(memberName).memberExprGenerator(this);
    }
  }
  
}
