package org.renjin.gcc.codegen.type.record.unit;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.WrappedFatPtrExpr;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;
import java.util.List;

public class RecordUnitPtrValueFunction implements ValueFunction {
  
  private Type recordType;

  public RecordUnitPtrValueFunction(Type recordType) {
    this.recordType = recordType;
  }

  @Override
  public Type getValueType() {
    return recordType;
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
  public Optional<JExpr> getValueConstructor() {
    return Optional.absent();
  }

  @Override
  public GExpr dereference(JExpr array, JExpr offset) {
    JExpr pointerValue = Expressions.elementAt(array, offset);
    JExpr castedPointerValue = Expressions.cast(pointerValue, recordType);
    FatPtrPair pointerAddress = new FatPtrPair(this, array, offset);
    
    return new RecordUnitPtr(castedPointerValue, pointerAddress);
  }

  @Override
  public GExpr dereference(WrappedFatPtrExpr wrapperInstance) {
    return new RecordUnitPtr(wrapperInstance.valueExpr(), wrapperInstance);
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    return Collections.singletonList((JExpr)expr);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, JExpr destinationArray, JExpr destinationOffset, JExpr sourceArray, JExpr sourceOffset, JExpr valueCount) {
    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset, valueCount);
  }

  @Override
  public String toString() {
    return "RecordUnitPtr[" + recordType + "]";
  }
}
