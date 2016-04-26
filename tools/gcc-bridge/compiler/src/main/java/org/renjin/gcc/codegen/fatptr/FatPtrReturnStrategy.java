package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Strategy for returning fat pointers from methods.  
 * 
 * <p>We cannot unfortunately return both the array and offset from a JVM method, so they need to
 * be wrapped in a xxxPtr object.
 */
public class FatPtrReturnStrategy implements ReturnStrategy {

  private ValueFunction valueFunction;

  public FatPtrReturnStrategy(ValueFunction valueFunction) {
    this.valueFunction = valueFunction;
  }

  @Override
  public Type getType() {
    return Wrappers.wrapperType(valueFunction.getValueType());
  }

  @Override
  public GimpleType getGimpleType() {
    return valueFunction.getGimpleValueType().pointerTo();
  }

  @Override
  public SimpleExpr marshall(Expr expr) {
    FatPtrExpr fatPtr = (FatPtrExpr) expr;
    return fatPtr.wrap();
  }

  @Override
  public Expr unmarshall(MethodGenerator mv, SimpleExpr returnValue) {
    // Store the returned Ptr wrapper to a local variable
    SimpleLValue wrapper = mv.getLocalVarAllocator().reserve("retval", returnValue.getType());
    wrapper.store(mv, returnValue);

    SimpleExpr array = Wrappers.arrayField(wrapper, valueFunction.getValueType());
    SimpleExpr offset = Wrappers.offsetField(wrapper);

    return new FatPtrExpr(array, offset);
  }

  @Override
  public SimpleExpr getDefaultReturnValue() {
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    return new FatPtrExpr(Expressions.nullRef(arrayType)).wrap();
  }
}
