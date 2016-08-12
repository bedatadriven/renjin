package org.renjin.gcc.codegen.array;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.WrappedFatPtrExpr;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.repackaged.asm.Type;

import java.util.List;


public class ArrayValueFunction implements ValueFunction {

  private final GimpleArrayType arrayType;
  private final ValueFunction elementValueFunction;

  public ArrayValueFunction(GimpleArrayType arrayType, ValueFunction elementValueFunction) {
    this.arrayType = arrayType;
    this.elementValueFunction = elementValueFunction;
  }

  @Override
  public Type getValueType() {
    return elementValueFunction.getValueType();
  }

  @Override
  public int getElementLength() {
    return elementValueFunction.getElementLength() * arrayType.getElementCount();
  }

  @Override
  public int getArrayElementBytes() {
    return elementValueFunction.getArrayElementBytes();
  }

  @Override
  public GExpr dereference(JExpr array, JExpr offset) {
    return new ArrayExpr(elementValueFunction, arrayType.getElementCount(), array, offset);
  }

  @Override
  public GExpr dereference(WrappedFatPtrExpr wrapperInstance) {
    return new ArrayExpr(elementValueFunction, arrayType.getElementCount(), 
        wrapperInstance.getArray(),
        wrapperInstance.getOffset());
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    return elementValueFunction.toArrayValues(expr);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, 
                         JExpr destinationArray, JExpr destinationOffset, 
                         JExpr sourceArray, JExpr sourceOffset, 
                         JExpr valueCount) {

    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset, valueCount);
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return elementValueFunction.getValueConstructor();
  }

  @Override
  public String toString() {
    return "Array[" + elementValueFunction + "]";
  }
}