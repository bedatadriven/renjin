package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import java.util.List;

/**
 * Translates a pointer array and offset to a Record value represented by a primitive array.
 */
public class RecordArrayValueFunction implements ValueFunction {

  
  private GimplePrimitiveType fieldType;
  private int arrayLength;

  public RecordArrayValueFunction(GimplePrimitiveType fieldType, int fieldCount) {
    this.fieldType = fieldType;
    this.arrayLength = fieldCount;
  }

  @Override
  public Type getValueType() {
    return fieldType.jvmType();
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
  public int getElementSize() {
    return fieldType.sizeOf() * arrayLength;
  }

  @Override
  public Optional<SimpleExpr> getValueConstructor() {
    // No constructor is required for individual fields;
    // they will default to zero.
    return Optional.absent();
  }

  @Override
  public Expr dereference(final SimpleExpr array, final SimpleExpr offset) {
    return new RecordArrayExpr() {
      @Override
      public SimpleExpr getArray() {
        return array;
      }

      @Override
      public SimpleExpr getOffset() {
        return offset;
      }

      @Override
      public SimpleExpr arrayForReturning() {
        return Expressions.copyOfArrayRange(array, offset, Expressions.sum(offset, arrayLength));
      }

      @Override
      public Expr addressOf() {
        return new FatPtrExpr(array, offset);
      }

      @Override
      public void store(MethodGenerator mv, Expr rhs) {
        RecordArrayExpr value = (RecordArrayExpr) rhs;
        mv.arrayCopy(value.getArray(), value.getOffset(), array, offset, Expressions.constantInt(arrayLength));
      }
    };
  }

  @Override
  public List<SimpleExpr> toArrayValues(Expr expr) {
    throw new UnsupportedOperationException("TODO");
  }
}
