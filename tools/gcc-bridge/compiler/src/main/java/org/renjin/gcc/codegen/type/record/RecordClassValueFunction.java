package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ArrayElement;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;

import java.util.Collections;
import java.util.List;


/**
 * Translates a pointer array and offset to a Record value represented by a JVM Class.
 */
public class RecordClassValueFunction implements ValueFunction {
  
  private RecordClassTypeStrategy strategy;

  public RecordClassValueFunction(RecordClassTypeStrategy strategy) {
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
  public int getArrayElementBytes() {
    return 4;
  }

  @Override
  public RecordValue dereference(JExpr array, JExpr offset) {
    ArrayElement element = Expressions.elementAt(array, offset);
    FatPtrExpr address = new FatPtrExpr(array, offset);
    
    return new RecordValue(element, address);
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    return Collections.singletonList(((RecordValue) expr).getRef());
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return Optional.<JExpr>of(new RecordConstructor(strategy));
  }

  @Override
  public String toString() {
    return "RecordClass[" + strategy.getRecordTypeDef().getName() + "]";
  }
}
