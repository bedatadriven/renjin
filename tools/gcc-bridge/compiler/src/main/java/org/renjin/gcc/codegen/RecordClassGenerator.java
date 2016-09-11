package org.renjin.gcc.codegen;

import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.annotations.GccSize;
import org.renjin.gcc.codegen.expr.ArrayElement;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.record.RecordClassLayout;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.asm.*;
import org.renjin.repackaged.asm.util.TraceClassVisitor;
import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.renjin.repackaged.asm.Opcodes.*;
import static org.renjin.repackaged.asm.Type.INT_TYPE;
import static org.renjin.repackaged.asm.Type.VOID_TYPE;


/**
 * Generates a JVM class for a {@link GimpleRecordTypeDef}
 */
public class RecordClassGenerator {

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;
  private Type className;
  private Type superClassName;
  private int size;
  private List<RecordClassLayout.Field> fields;

  public RecordClassGenerator(Type className, Type superClassName, List<RecordClassLayout.Field> fields, int size) {
    this.size = size;
    this.fields = fields;
    this.className = className;
    this.superClassName = superClassName;
  }

  public void writeClassFile(File outputDirectory) throws IOException {
    File classFile = new File(outputDirectory.getAbsolutePath() + File.separator + className.getInternalName()  + ".class");
    if(!classFile.getParentFile().exists()) {
      boolean created = classFile.getParentFile().mkdirs();
      if(!created) {
        throw new IOException("Failed to create directory for class file: " + classFile.getParentFile());
      }
    }
    Files.write(generateClassFile(), classFile);
  }

  public byte[] generateClassFile() throws IOException {

    sw = new StringWriter();
    pw = new PrintWriter(sw);
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    if(GimpleCompiler.TRACE) {
      cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    } else {
      cv = cw;
    }
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, 
        className.getInternalName(), null,
        superClassName.getInternalName(), new String[0]);

    AnnotationVisitor annotationVisitor = cv.visitAnnotation(Type.getDescriptor(GccSize.class), true);
    annotationVisitor.visit("value", size);
    annotationVisitor.visitEnd();

    emitDefaultConstructor();
    emitFields();
    emitSetMethod();
    emitCloneMethod();
    emitMemSetMethod();
    emitMemSetArrayMethod();
    cv.visitEnd();

    return cw.toByteArray();
  }

  private void emitFields() {
    for (RecordClassLayout.Field field : fields) {
      field.getStrategy().writeFields(cv);
    }
  }

  /**
   * Generates a method which sets *This* fields values from another Record instance
   */
  private void emitSetMethod() {
    String descriptor = Type.getMethodDescriptor(VOID_TYPE, className);
    MethodGenerator mv = new MethodGenerator(cv.visitMethod(ACC_PUBLIC, "set", descriptor, null, null));
    mv.visitCode();
    
    // Local variables
    JExpr thisExpr = mv.getLocalVarAllocator().reserve(className);
    JExpr sourceExpr = mv.getLocalVarAllocator().reserve(className);
    
    // Now copy each field member
    for (RecordClassLayout.Field field : fields) {
      try {
        field.getStrategy().copy(mv, sourceExpr, thisExpr);
      } catch (Exception e) {
        throw new InternalCompilerException("Exception generating copy code for " + className + ", field: " + field, e);
      }
    }
    
    mv.areturn(VOID_TYPE);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
  
  private void emitMemSetMethod() {
    // Signature: void memset(int c, int bytes)
    String descriptor = Type.getMethodDescriptor(VOID_TYPE, INT_TYPE, INT_TYPE);
    MethodGenerator mv = new MethodGenerator(cv.visitMethod(ACC_PUBLIC, "memset", descriptor, null, null));
    mv.visitCode();

    JExpr thisExpr = mv.getLocalVarAllocator().reserve(className);
    JExpr valueExpr = mv.getLocalVarAllocator().reserve(INT_TYPE);
    LocalVarAllocator.LocalVar byteCountExpr = mv.getLocalVarAllocator().reserve(INT_TYPE);
    LocalVarAllocator.LocalVar fieldByteCount = mv.getLocalVarAllocator().reserve(INT_TYPE);
    
    Label exit = new Label();
    
    // Set fields
    for (RecordClassLayout.Field field : fields) {
      
      // compute the number of bytes to copy of this field
      
      // subtract the field's offset from the number of bytes to copy
      byteCountExpr.load(mv);
      mv.iconst(field.getOffsetInBytes());
      mv.sub(Type.INT_TYPE);

      // Now limit the number of bytes to copy to the field's declared length.
      mv.iconst(field.getSizeInBytes());
      mv.invokestatic(Math.class, "min", Type.getMethodDescriptor(Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE));
      
      mv.dup();
      mv.store(fieldByteCount.getIndex(), Type.INT_TYPE);
      
      // if count <= 0, stop setting 
      mv.iflt(exit);
      
      // memset() the field
      try {
        field.getStrategy().memset(mv, thisExpr, valueExpr, fieldByteCount);
      } catch (Exception e) {
        throw new InternalCompilerException("Exception generating field memset code for " + field + " in " + 
            className, e);
      }
    }
    mv.mark(exit);
    mv.areturn(Type.VOID_TYPE);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
  
  private void emitMemSetArrayMethod() {
    // Signature: void memset(record[] array, int offset, int c, int bytes)
    Type arrayType = Type.getType("[" + className.getDescriptor());
    String descriptor = Type.getMethodDescriptor(VOID_TYPE, arrayType, INT_TYPE, 
        INT_TYPE, INT_TYPE);

    MethodGenerator mv = new MethodGenerator(cv.visitMethod(ACC_PUBLIC | ACC_STATIC, 
        "memset", descriptor, null, null));
    
    mv.visitCode();
    
    // Parameters
    JExpr arrayExpr = mv.getLocalVarAllocator().reserve(arrayType);
    LocalVarAllocator.LocalVar offsetExpr = mv.getLocalVarAllocator().reserve(INT_TYPE);
    JExpr valueExpr = mv.getLocalVarAllocator().reserve(INT_TYPE);
    LocalVarAllocator.LocalVar byteCountExpr = mv.getLocalVarAllocator().reserve(INT_TYPE);
    
    // Loop while we have bytes left
    Label loopBody = new Label();
    mv.mark(loopBody);
    
    // Invoke the this.memset(value, count);
    ArrayElement instanceExpr = Expressions.elementAt(arrayExpr, offsetExpr);

    instanceExpr.load(mv);
    valueExpr.load(mv);
    byteCountExpr.load(mv);
    mv.invokevirtual(className, "memset", Type.getMethodDescriptor(VOID_TYPE, INT_TYPE, INT_TYPE), false);
    
    // increment the array offset and decrement bytes to set
    mv.iinc(offsetExpr.getIndex(), 1);
    mv.iinc(byteCountExpr.getIndex(), -size);
    
    // Check to see if we've need to set more
    byteCountExpr.load(mv);
    mv.ifgt(loopBody);

    mv.areturn(Type.VOID_TYPE);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
  
  private void emitCloneMethod() {
    String cloneDescriptor = Type.getMethodDescriptor(className);
    MethodGenerator mv = new MethodGenerator(cv.visitMethod(ACC_PUBLIC, "clone", cloneDescriptor, null, null));
    mv.visitCode();

    // Reserve the first local variable for the 'this' pointer
    mv.getLocalVarAllocator().reserve(className);

    // Create copy
    mv.anew(className);
    mv.dup();
    mv.invokeconstructor(className);
    mv.store(1, className);
    
    
    // Set the fields from one to the other
    mv.load(1, className);
    mv.load(0, className);
    mv.invokevirtual(className, "set", Type.getMethodDescriptor(VOID_TYPE, className), false);
    
    mv.load(1, className);
    mv.areturn(className);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }


  private void emitDefaultConstructor() {
    MethodGenerator mv = new MethodGenerator(cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null));
    
    // Reserve the first local variable for the 'this' pointer
    mv.getLocalVarAllocator().reserve(className);
    
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, superClassName.getInternalName(), "<init>", "()V", false);

    for (RecordClassLayout.Field field : fields) {
      field.getStrategy().emitInstanceInit(mv);
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
}
