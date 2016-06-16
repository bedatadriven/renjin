package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;

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
  public JExpr marshall(GExpr expr) {
    FatPtrExpr fatPtr = (FatPtrExpr) expr;
    return fatPtr.wrap();
  }

  @Override
  public GExpr unmarshall(MethodGenerator mv, JExpr returnValue, TypeStrategy lhsTypeStrategy) {
    // Store the returned Ptr wrapper to a local variable
    JLValue wrapper = mv.getLocalVarAllocator().reserve("retval", returnValue.getType());
    wrapper.store(mv, returnValue);

    JExpr array = Wrappers.arrayField(wrapper, valueFunction.getValueType());
    JExpr offset = Wrappers.offsetField(wrapper);

    return new FatPtrExpr(array, offset);
  }

  @Override
  public JExpr getDefaultReturnValue() {
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    return new FatPtrExpr(Expressions.nullRef(arrayType)).wrap();
  }
}
