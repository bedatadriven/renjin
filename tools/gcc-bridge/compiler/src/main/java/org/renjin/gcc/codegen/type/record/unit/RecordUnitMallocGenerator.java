package org.renjin.gcc.codegen.type.record.unit;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.type.record.RecordTypeStrategy;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Allocates a new Record
 */
public class RecordUnitMallocGenerator extends AbstractExprGenerator {
  private RecordTypeStrategy strategy;

  public RecordUnitMallocGenerator(RecordTypeStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public GimpleType getGimpleType() {
    return strategy.getRecordType().pointerTo();
  }

  @Override
  public void emitPushRecordRef(MethodVisitor mv) { 
    strategy.emitConstructor(mv);
  }
}
