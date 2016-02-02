package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;


public class RecordValue implements Value {
  
  private RecordTypeStrategy strategy;
  private Value instanceValue;

  public RecordValue(RecordTypeStrategy strategy, Value instanceValue) {
    this.strategy = strategy;
    this.instanceValue = instanceValue;
  }

  @Override
  public Type getType() {
    return instanceValue.getType();
  }

  @Override
  public void load(MethodGenerator mv) {
    instanceValue.load(mv);
  }
}
