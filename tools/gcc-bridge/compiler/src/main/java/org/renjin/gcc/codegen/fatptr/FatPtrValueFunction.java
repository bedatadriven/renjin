package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;
import java.util.List;

/**
 * Dereferences from double** -> double *
 */
public class FatPtrValueFunction implements ValueFunction {

  private final ValueFunction baseValueFunction;
  private final Type arrayType;

  public FatPtrValueFunction(ValueFunction baseValueFunction) {
    this.baseValueFunction = baseValueFunction;
    this.arrayType = Wrappers.valueArrayType(baseValueFunction.getValueType());
  }
  
  @Override
  public Type getValueType() {
    return Wrappers.wrapperType(baseValueFunction.getValueType());
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getArrayElementBytes() {
    return 4;
  }

  /**
   * Dereferences a FatPtr from an ObjectPtr[]
   * @param array
   * @param offset
   * @return
   */
  @Override
  public GExpr dereference(JExpr array, JExpr offset) {
    return new DereferencedFatPtr(array, offset, baseValueFunction);
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    FatPtrPair fatPtrExpr = (FatPtrPair) expr;
    return Collections.singletonList(fatPtrExpr.wrap());
  }

  @Override
  public void memoryCopy(MethodGenerator mv, 
                         JExpr destinationArray, JExpr destinationOffset, 
                         JExpr sourceArray, JExpr sourceOffset, JExpr valueCount) {
    
    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset, valueCount);
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return Optional.of(FatPtrPair.nullPtr(baseValueFunction).wrap());
  }

  @Override
  public String toString() {
    return "FatPtr[" + baseValueFunction + "]";
  }
}
