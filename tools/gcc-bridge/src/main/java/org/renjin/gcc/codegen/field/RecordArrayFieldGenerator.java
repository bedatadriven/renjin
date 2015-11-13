package org.renjin.gcc.codegen.field;

import com.google.common.base.Preconditions;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PrimitiveConstValueGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
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

  @Override
  public void emitStaticInitializer(MethodVisitor mv, GimpleConstructor expr) {
    Preconditions.checkArgument(arrayType.getElementCount() >= 0);
    
    PrimitiveConstValueGenerator.emitInt(mv, arrayType.getElementCount());
    mv.visitTypeInsn(Opcodes.ANEWARRAY, recordGenerator.getClassName());
    
    for(int i=0;i<arrayType.getElementCount();++i) {
      // duplicate the array to keep it on the stack
      mv.visitInsn(Opcodes.DUP);

      // array index
      PrimitiveConstValueGenerator.emitInt(mv, i);
      
      recordGenerator.emitConstructor(mv, expr.<GimpleConstructor>getElement(i));
      
      mv.visitInsn(Opcodes.AASTORE);
    }
    
    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, fieldDescriptor);
    
    mv.visitEnd();
    
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
  
}
