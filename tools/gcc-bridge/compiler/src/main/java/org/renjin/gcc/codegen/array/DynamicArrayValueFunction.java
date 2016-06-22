package org.renjin.gcc.codegen.array;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;

import java.util.List;

public class DynamicArrayValueFunction implements ValueFunction {
  
  private ValueFunction elementValueFunction;

  public DynamicArrayValueFunction(ValueFunction elementValueFunction) {
    this.elementValueFunction = elementValueFunction;
  }

  @Override
  public Type getValueType() {
    return elementValueFunction.getValueType();
  }

  @Override
  public int getElementLength() {
    return elementValueFunction.getElementLength();
  }

  @Override
  public int getArrayElementBytes() {
    return elementValueFunction.getArrayElementBytes();
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return elementValueFunction.getValueConstructor();
  }

  @Override
  public GExpr dereference(JExpr array, JExpr offset) {
    return new DynamicArrayExpr(array, offset);
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void memoryCopy(MethodGenerator mv, 
                         JExpr destinationArray, JExpr destinationOffset, 
                         JExpr sourceArray, JExpr sourceOffset, JExpr valueCount) {
    throw new UnsupportedOperationException("TODO");
  }
}
