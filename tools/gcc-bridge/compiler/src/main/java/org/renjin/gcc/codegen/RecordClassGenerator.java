package org.renjin.gcc.codegen;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

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
  private Type superClassName;
  private Collection<FieldStrategy> fields;

  public RecordClassGenerator(Type className, Type superClassName, Collection<FieldStrategy> fields) {
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

    emitDefaultConstructor();
    emitFields();
    cv.visitEnd();

    return cw.toByteArray();
  }

  private void emitFields() {
    for (FieldStrategy fieldStrategy : fields) {
      fieldStrategy.writeFields(cv);
    }
  }

  private void emitDefaultConstructor() {
    MethodGenerator mv = new MethodGenerator(cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null));
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, superClassName.getInternalName(), "<init>", "()V", false);

    for (FieldStrategy fieldStrategy : fields) {
      fieldStrategy.emitInstanceInit(mv);
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
}
