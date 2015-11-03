package org.renjin.gcc.codegen;

import com.google.common.collect.Maps;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.gimple.type.GimpleType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;


public class RecordClassGenerator {

  private ClassWriter cw;
  private ClassVisitor cv;
  private StringWriter sw;
  private PrintWriter pw;
  private String className;

  private final GimpleRecordTypeDef recordType;
  private final Map<String, FieldGenerator> fields = Maps.newHashMap();

  public RecordClassGenerator(String packageName, String className, GimpleRecordTypeDef recordType) {
    this.className = packageName.replace('.', '/') + "/" + className;
    this.recordType = recordType;
    
    GeneratorFactory factory = new GeneratorFactory();
    for (GimpleField gimpleField : recordType.getFields()) {
      fields.put(gimpleField.getName(), factory.forField(this.className, gimpleField.getName(), gimpleField.getType()));
    }
  }

  public void emitRecordClass(GimpleRecordTypeDef record) {
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[0]);

    emitDefaultConstructor();
    emitFields();
    cv.visitEnd();
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

  public byte[] toByteArray() {
    cv.visitEnd();
    return cw.toByteArray();
  }

  public GimpleType getGimpleType() {
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
    FieldGenerator fieldGenerator = fields.get(name);
    if(fieldGenerator == null) {
      throw new InternalCompilerException(String.format("No field named '%s' in record type '%s'", name, className));
    }
    return fieldGenerator;
  }
}
