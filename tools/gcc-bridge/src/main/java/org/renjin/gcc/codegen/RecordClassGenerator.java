package org.renjin.gcc.codegen;

import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;


public class RecordClassGenerator {

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;
  private String className;

  private final GimpleRecordTypeDef recordType;

  private Map<String, FieldGenerator> fields = null;
  private GeneratorFactory factory;

  public RecordClassGenerator(GeneratorFactory factory, String className, GimpleRecordTypeDef recordType) {
    this.factory = factory;
    this.className = className;
    this.recordType = recordType;
    
    if(recordType.isUnion()) {
      throw new UnsupportedOperationException("union types not yet implemented.");
    }
  }
  
  public GimpleRecordTypeDef getTypeDef() {
    return recordType;
  }


  public void linkFields() {
    fields = new HashMap<>();
    for (GimpleField gimpleField : recordType.getFields()) {
      FieldGenerator fieldGenerator = factory.forField(this.className, gimpleField.getName(), gimpleField.getType());
      fields.put(gimpleField.getName(), fieldGenerator);
    }
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
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[0]);

    emitDefaultConstructor();
    emitFields();
    cv.visitEnd();
    
    return cw.toByteArray();
  }

  private void emitFields() {
    for (FieldGenerator fieldGenerator : fields.values()) {
      fieldGenerator.emitInstanceField(cv);
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

  public GimpleRecordType getGimpleType() {
    GimpleRecordType recordType = new GimpleRecordType();
    recordType.setId(this.recordType.getId());
    recordType.setName(this.recordType.getName());
    recordType.setSize(computeSize());
    return recordType;
  }

  private int computeSize() {
    int size = 0;
    for (GimpleField gimpleField : recordType.getFields()) {
      size += gimpleField.getType().sizeOf() * 8;
    }
    return size;
  }

  public String getClassName() {
    return className;
  }

  public String getDescriptor() {
    return "L" + className + ";";
  }
  
  public Type getType() {
    return Type.getType(getDescriptor());
  }

  public FieldGenerator getFieldGenerator(String name) {
    if(fields == null) {
      throw new IllegalStateException("Fields map is not yet initialized.");
    }
    FieldGenerator fieldGenerator = fields.get(name);
    if(fieldGenerator == null) {
      throw new InternalCompilerException(String.format("No field named '%s' in record type '%s'", name, className));
    }
    return fieldGenerator;
  }

  public void emitConstructor(MethodVisitor mv) {
    mv.visitTypeInsn(Opcodes.NEW, className);
    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, className, "<init>", "()V", false);
  }
  
  public void emitConstructor(MethodVisitor mv, GimpleConstructor recordConstructor) {
    emitConstructor(mv);

    for (GimpleConstructor.Element element : recordConstructor.getElements()) {
      FieldGenerator fieldGenerator = getFieldGenerator(element.getFieldName());
    }
    // TODO
  }
}
