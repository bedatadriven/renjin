package org.renjin.gcc.codegen;

import com.google.common.io.Files;
import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;


/**
 * Generates a JVM class for a {@link GimpleRecordTypeDef}
 */
public class RecordClassGenerator {

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;
  private Type className;
  private Collection<FieldGenerator> fields;

  public RecordClassGenerator(Type className, Collection<FieldGenerator> fields) {
    this.fields = fields;
    this.className = className;
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
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className.getInternalName(), null, "java/lang/Object", new String[0]);

    emitDefaultConstructor();
    emitFields();
    cv.visitEnd();

    return cw.toByteArray();
  }

  private void emitFields() {
    for (FieldGenerator fieldGenerator : fields) {
      fieldGenerator.emitInstanceField(cv);
    }
  }

  private void emitDefaultConstructor() {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

    for (FieldGenerator fieldGenerator : fields) {
      fieldGenerator.emitInstanceInit(mv);
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
}
