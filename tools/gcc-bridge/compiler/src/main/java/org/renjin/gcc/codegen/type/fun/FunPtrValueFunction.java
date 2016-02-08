package org.renjin.gcc.codegen.type.fun;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;

import java.lang.invoke.MethodHandle;
import java.util.Collections;
import java.util.List;


public class FunPtrValueFunction implements ValueFunction {
  
  private final int pointerSize;

  /**
   * 
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
  public int getElementSize() {
    return pointerSize;
  }

  @Override
  public Expr dereference(SimpleExpr array, SimpleExpr offset) {
    return Expressions.elementAt(array, offset);
  }

  @Override
  public List<SimpleExpr> getDefaultValue() {
    return Collections.emptyList();
  }
}
