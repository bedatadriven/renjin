package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Var;

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
  public Value marshall(ExprGenerator expr) {
    FatPtrExpr fatPtr = (FatPtrExpr) expr;
    return fatPtr.wrap();
  }

  @Override
  public ExprGenerator unmarshall(MethodGenerator mv, Value returnValue) {
    // Store the returned Ptr wrapper to a local variable
    Var wrapper = mv.getLocalVarAllocator().reserve("retval", returnValue.getType());
    wrapper.store(mv, returnValue);

    Value array = Wrappers.arrayField(wrapper, valueFunction.getValueType());
    Value offset = Wrappers.offsetField(wrapper);

    return new FatPtrExpr(array, offset);
  }
}
