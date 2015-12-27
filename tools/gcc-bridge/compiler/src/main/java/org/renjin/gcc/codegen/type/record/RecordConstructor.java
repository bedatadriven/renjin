package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.Collections;
import java.util.Map;

public class RecordConstructor extends AbstractExprGenerator {
  
  private RecordClassGenerator generator;
  private Map<String, ExprGenerator> fields;

  public RecordConstructor(RecordClassGenerator generator, Map<String, ExprGenerator> fields) {
    this.generator = generator;
    this.fields = fields;
  }
  
  public RecordConstructor(RecordClassGenerator generator) {
    this(generator, Collections.<String, ExprGenerator>emptyMap());
  }

  @Override
  public GimpleType getGimpleType() {
    return generator.getGimpleType();
  }
  
  @Override
  public void emitPushRecordRef(MethodVisitor mv) {
    mv.visitTypeInsn(Opcodes.NEW, generator.getClassName());
    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, generator.getClassName(), "<init>", "()V", false);

    for (Map.Entry<String, ExprGenerator> field : fields.entrySet()) {
      // Keep the record on the stack
      mv.visitInsn(Opcodes.DUP);

      // Push the value onto the stack and save to the field
      FieldGenerator fieldGenerator = generator.getFieldGenerator(field.getKey());
      fieldGenerator.emitStoreMember(mv, field.getValue());
    }
  }
}
