package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

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
  public Optional<SimpleExpr> getValueConstructor() {
    // No constructor is required for individual fields;
    // they will default to zero.
    return Optional.absent();
  }

  @Override
  public Expr dereference(final SimpleExpr array, final SimpleExpr offset) {
    return new RecordArrayExpr(array, offset, arrayLength);
  }

  @Override
  public List<SimpleExpr> toArrayValues(Expr expr) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public String toString() {
    return "RecordArray[" + fieldType + "]";
  }
}

