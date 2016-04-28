package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;

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
  public int getElementSize() {
    return 4;
  }

  /**
   * Dereferences a FatPtr from an ObjectPtr[]
   * @param array
   * @param offset
   * @return
   */
  @Override
  public Expr dereference(SimpleExpr array, SimpleExpr offset) {
    // DoublePtr[] array
    // int offset
    // double[] unwrappedArray = array[offset].array
    // int unwrappedOffset = array[offset].offset
    
    FatPtrExpr address = new FatPtrExpr(array, offset);
    SimpleExpr wrapperInstance = Expressions.elementAt(array, offset);
    
    SimpleExpr unwrappedArray = Wrappers.arrayField(wrapperInstance, baseValueFunction.getValueType());
    SimpleExpr unwrappedOffset = Wrappers.offsetField(wrapperInstance);

    return new FatPtrExpr(address, unwrappedArray, unwrappedOffset);
  }

  @Override
  public List<SimpleExpr> toArrayValues(Expr expr) {
    FatPtrExpr fatPtrExpr = (FatPtrExpr) expr;
    return Collections.singletonList(fatPtrExpr.wrap());
  }

  @Override
  public Optional<SimpleExpr> getValueConstructor() {
    return Optional.of(FatPtrExpr.nullPtr(baseValueFunction).wrap());
  }
}
