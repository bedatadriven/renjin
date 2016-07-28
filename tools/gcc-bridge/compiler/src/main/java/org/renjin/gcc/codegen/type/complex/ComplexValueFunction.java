package org.renjin.gcc.codegen.type.complex;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.repackaged.asm.Type;

import java.util.List;

public class ComplexValueFunction implements ValueFunction {
  
  private final Type valueType;

  public ComplexValueFunction(Type valueType) {
    Preconditions.checkArgument(valueType.equals(Type.DOUBLE_TYPE) || valueType.equals(Type.FLOAT_TYPE));
    this.valueType = valueType;
  }

  @Override
  public Type getValueType() {
    return valueType;
  }

  @Override
  public int getElementLength() {
    return 2;
  }

  @Override
  public int getArrayElementBytes() {
    if(valueType.equals(Type.DOUBLE_TYPE)) {
      return 16; 
    } else {
      return 8;
    }
  }

  @Override
  public GExpr dereference(JExpr array, JExpr offset) {

    FatPtrPair address = new FatPtrPair(array, offset);
    
    // Real element is at i
    JExpr realOffset = offset;
    // Complex element is at i+1
    JExpr imaginaryOffset = Expressions.sum(realOffset, Expressions.constantInt(1));
    
    JExpr real = Expressions.elementAt(array, realOffset);
    JExpr imaginary = Expressions.elementAt(array, imaginaryOffset);
    
    return new ComplexValue(address, real, imaginary);
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    ComplexValue value = (ComplexValue) expr;
    return Lists.newArrayList(value.getRealJExpr(), value.getImaginaryJExpr());
  }

  @Override
  public void memoryCopy(MethodGenerator mv, 
                         JExpr destinationArray, JExpr destinationOffset, 
                         JExpr sourceArray, JExpr sourceOffset, 
                         JExpr valueCount) {
    
    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset, 
        Expressions.product(valueCount, 2));
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return Optional.absent();
  }

  @Override
  public String toString() {
    return "Complex[" + valueType + "]";
  }
}
