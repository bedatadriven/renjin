package org.renjin.gcc.codegen;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.gimple.GimpleFunction;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.objectweb.asm.Opcodes.*;


public class ClassGenerator {

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;
  private String className;

  public ClassGenerator(String className) {

    this.className = className;
  }
  
  public void emit() {
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[0]);
    
    emitDefaultConstructor();
  }

  private void emitDefaultConstructor() {
    MethodVisitor mv = cv.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  public void emitFunction(GimpleFunction function) {
    try {
      FunctionGenerator functionGenerator = new FunctionGenerator(null, function);
      functionGenerator.emit(cv);
    } catch (Exception e) {
      throw new RuntimeException("Exception generating function " + function.getName(), e);
    }
  }
  
  public byte[] toByteArray() {
    cv.visitEnd();
    return cw.toByteArray();
  }
}
