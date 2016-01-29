package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.Map;

public class RecordConstructor extends AbstractExprGenerator {
  
  private RecordClassTypeStrategy strategy;
  private Map<String, ExprGenerator> fields;

  public RecordConstructor(RecordClassTypeStrategy strategy, Map<String, ExprGenerator> fields) {
    this.strategy = strategy;
    this.fields = fields;
  }

  @Override
  public GimpleType getGimpleType() {
    return strategy.getRecordType();
  }
  
  @Override
  public void emitPushRecordRef(MethodVisitor mv) {
    mv.visitTypeInsn(Opcodes.NEW, strategy.getJvmType().getInternalName());
    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, strategy.getJvmType().getInternalName(), "<init>", "()V", false);

    for (Map.Entry<String, ExprGenerator> field : fields.entrySet()) {
      // Keep the record on the stack
      mv.visitInsn(Opcodes.DUP);

      // Push the value onto the stack and save to the field
      FieldGenerator fieldGenerator = strategy.getFieldGenerator(field.getKey());
      fieldGenerator.emitStoreMember(mv, field.getValue());
    }
  }
}
