package org.renjin.gcc.codegen;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.objectweb.asm.Opcodes.*;


public class RecordClassGenerator {

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;
  private String className;

  public RecordClassGenerator(String className) {
    this.className = className;
  }
  
  public void emit(GimpleRecordTypeDef record) {
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[0]);

     emitDefaultConstructor();
     emitFields(record);
    cv.visitEnd();
  }

  private void emitFields(GimpleRecordTypeDef record){
    for (GimpleField gimpleField : record.getFields()) {
      FieldVisitor fv = cv.visitField(ACC_PUBLIC, gimpleField.getName(), "I", null, null);
      fv.visitEnd();
    }
  }


  private void emitDefaultConstructor() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  public byte[] toByteArray() {
    cv.visitEnd();
    return cw.toByteArray();
  }
}
