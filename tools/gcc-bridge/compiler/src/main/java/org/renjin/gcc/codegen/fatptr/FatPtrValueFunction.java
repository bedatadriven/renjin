package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.util.Collections;
import java.util.List;

import static org.renjin.gcc.codegen.expr.Expressions.constantInt;
import static org.renjin.gcc.codegen.expr.Expressions.divide;

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
  public GExpr dereference(WrappedFatPtrExpr wrapperInstance) {
    return new DereferencedWrappedFatPtr(baseValueFunction, wrapperInstance);
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
  public void memorySet(MethodGenerator mv, JExpr array, JExpr offset, JExpr byteValue, JExpr length) {
    // `array` is an array of wrappers, for example 
    // ObjectPtr[],
    // DoublePtr[], etc. 
    
    // Since we know that byteValue will never result in a valid pointer, then we will set them
    // all to NULL with
    // Arrays.fill(array, fromIndex, toIndex, DoublePtr.NULL)
    
    JExpr toIndex = Expressions.sum(offset, divide(length, constantInt(GimplePointerType.SIZE / 8)));
    JLValue nullInstance = Expressions.staticField(getValueType(), "NULL", getValueType());

    mv.fillArray(array, offset, toIndex, nullInstance);
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
