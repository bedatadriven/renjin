package org.renjin.gcc.codegen.type.record.unit;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Allocates a new Record
 */
public class RecordUnitMallocGenerator extends AbstractExprGenerator {
  private RecordClassTypeStrategy strategy;

  public RecordUnitMallocGenerator(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public GimpleType getGimpleType() {
    return strategy.getRecordType().pointerTo();
  }

  @Override
  public void emitPushRecordRef(MethodGenerator mv) { 
    strategy.emitConstructor(mv);
  }
}
