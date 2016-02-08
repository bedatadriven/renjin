package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;

import java.util.Collections;
import java.util.List;


public class RecordValueFunction implements ValueFunction {
  
  private RecordClassTypeStrategy strategy;

  public RecordValueFunction(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public Type getValueType() {
    return strategy.getJvmType();
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getElementSize() {
    return 32;
  }

  @Override
  public Value dereference(Value array, Value offset) {
    return Values.elementAt(array, offset);
  }

  @Override
  public List<Value> getDefaultValue() {
    return Collections.<Value>singletonList(new RecordConstructor(strategy));
  }
}
