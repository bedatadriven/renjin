package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
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
    // Object[] array
    // int offset
    // DoublePtr wrapper = (DoublePtr)array[offset];
    // double[] unwrappedArray = wrapper.array
    // int unwrappedOffset = wrapper.offset
    
    // First we get the element from the array of Objects[]
    FatPtrPair address = new FatPtrPair(array, offset);
    JExpr element = Expressions.elementAt(array, offset);
    
    return new DereferencedFatPtr(array, offset, baseValueFunction);
//    
//    // Now cast the Object instance to an instance of e.g. DoublePtr
//    Type wrapperType = Wrappers.wrapperType(baseValueFunction.getValueType());
//    JExpr wrapperInstance = Expressions.cast(element, wrapperType);
//        
//    // Now we have references to the array and offset fields of the wrapper
//    JExpr unwrappedArray = Wrappers.arrayField(wrapperInstance, baseValueFunction.getValueType());
//    JExpr unwrappedOffset = Wrappers.offsetField(wrapperInstance);
//
//    return new FatPtrExpr(address, unwrappedArray, unwrappedOffset);
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
