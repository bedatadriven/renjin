package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.WrappedFatPtrExpr;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.repackaged.asm.Type;

import java.util.List;

/**
 * Translates a pointer array and offset to a Record value represented by a primitive array.
 */
public class RecordArrayValueFunction implements ValueFunction {

  
  private Type fieldType;
  private int arrayLength;

  public RecordArrayValueFunction(Type fieldType, int fieldCount) {
    this.fieldType = fieldType;
    this.arrayLength = fieldCount;
  }

  @Override
  public Type getValueType() {
    return fieldType;
  }

  /**
   * Returns the number of array elements required for each record value. 
   * 
   * <p>For example, a {@code struct} in C defined as: </p>
   * <pre>
   * struct point {
   *   double x;
   *   double y;
   * }  
   * </pre>
   * <p>Would require 2 elements in a {@code double[]} for each {@code point} value.</p>
   */
  @Override
  public int getElementLength() {
    return arrayLength;
  }

  @Override
  public int getArrayElementBytes() {
    return GimplePrimitiveType.fromJvmType(fieldType).sizeOf();
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    // No constructor is required for individual fields;
    // they will default to zero.
    return Optional.absent();
  }

  @Override
  public GExpr dereference(final JExpr array, final JExpr offset) {
    return new RecordArrayExpr(this, array, offset, arrayLength);
  }

  @Override
  public GExpr dereference(WrappedFatPtrExpr wrapperInstance) {
    return dereference(wrapperInstance.getArray(), wrapperInstance.getOffset());
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memoryCopy(MethodGenerator mv, JExpr destinationArray, JExpr destinationOffset, JExpr sourceArray, JExpr sourceOffset, JExpr valueCount) {
    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset, valueCount);
  }

  @Override
  public String toString() {
    return "RecordArray[" + fieldType + "]";
  }
}

