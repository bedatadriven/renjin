package org.renjin.gcc.codegen.type.fun;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.WrappedFatPtrExpr;
import org.renjin.repackaged.asm.Type;

import java.lang.invoke.MethodHandle;
import java.util.Collections;
import java.util.List;


public class FunPtrValueFunction implements ValueFunction {

  private final int pointerSize;

  /**
   *
   * @param functionType
   * @param pointerSize the size, in bytes, of the function pointer as understood by GCC. 
   */
  public FunPtrValueFunction(int pointerSize) {
    this.pointerSize = pointerSize;
  }

  @Override
  public Type getValueType() {
    return Type.getType(MethodHandle.class);
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getArrayElementBytes() {
    return pointerSize;
  }

  @Override
  public GExpr dereference(JExpr array, JExpr offset) {
    JExpr ptr = Expressions.cast(Expressions.elementAt(array, offset), Type.getType(MethodHandle.class));
    FatPtrPair address = new FatPtrPair(this, array, offset);
    return new FunPtr(ptr, address);
  }

  @Override
  public GExpr dereference(WrappedFatPtrExpr wrapperInstance) {
    return new FunPtr(wrapperInstance.valueExpr(), wrapperInstance);
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    return Collections.singletonList(((FunPtr) expr).unwrap());
  }

  @Override
  public void memoryCopy(MethodGenerator mv, JExpr destinationArray, JExpr destinationOffset, JExpr sourceArray, JExpr sourceOffset, JExpr valueCount) {
    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset, valueCount);
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return Optional.absent();
  }

  @Override
  public String toString() {
    return "FunPtr";
  }
}
