package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Generates a field with a record type
 */
public class RecordFieldGenerator extends FieldGenerator {
  private String className;
  private String fieldName;
  private RecordClassGenerator recordGenerator;

  public RecordFieldGenerator(String className, String fieldName, RecordClassGenerator recordGenerator) {
    this.className = className;
    this.fieldName = fieldName;
    this.recordGenerator = recordGenerator;
  }

  @Override
  public GimpleType getType() {
    return recordGenerator.getGimpleType();
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, recordGenerator.getDescriptor(), null, null).visitEnd();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    throw new InternalCompilerException("todo");
  }

}
