package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;

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
    return WrapperType.wrapperType(baseValueFunction.getValueType());
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getElementSize() {
    return 32;
  }

  /**
   * Dereferences a FatPtr from an ObjectPtr[]
   * @param array
   * @param offset
   * @return
   */
  @Override
  public ExprGenerator dereference(Value array, Value offset) {
    // DoublePtr[] array
    // int offset
    // double[] unwrappedArray = array[offset].array
    // int unwrappedOffset = array[offset].offset
    
    Value wrapperInstance = Values.elementAt(array, offset);
    
    Value unwrappedArray = Wrappers.arrayField(wrapperInstance, baseValueFunction.getValueType());
    Value unwrappedOffset = Wrappers.offsetField(wrapperInstance);

    return new FatPtrExpr(unwrappedArray, unwrappedOffset);
  }
}
