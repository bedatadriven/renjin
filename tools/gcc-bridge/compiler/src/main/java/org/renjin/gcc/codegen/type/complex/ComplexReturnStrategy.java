package org.renjin.gcc.codegen.type.complex;


import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.gimple.type.GimpleComplexType;

/**
 * Strategy for returning a complex value as a {@code double[2]} or {@code float[2]}
 */
public class ComplexReturnStrategy implements ReturnStrategy {
  
  private GimpleComplexType type;

  public ComplexReturnStrategy(GimpleComplexType type) {
    this.type = type;
  }

  @Override
  public Type getType() {
    return type.getJvmPartArrayType();
  }

  @Override
  public SimpleExpr marshall(Expr expr) {
    ComplexValue complexValue = (ComplexValue) expr;
    return Expressions.newArray(
        complexValue.getRealValue(),
        complexValue.getImaginaryValue());
  }

  @Override
  public Expr unmarshall(MethodGenerator mv, SimpleExpr returnValue) {
    // Allocate a temporary variable for the array so that it's 
    // components can be accessed
    SimpleLValue array = mv.getLocalVarAllocator().reserve("retval", returnValue.getType());
    array.store(mv, returnValue);
    SimpleExpr realValue = Expressions.elementAt(array, 0);
    SimpleExpr imaginaryValue = Expressions.elementAt(array, 1);
    
    return new ComplexValue(realValue, imaginaryValue);
  }

  @Override
  public SimpleExpr getDefaultReturnValue() {
    SimpleExpr zero = Expressions.zero(type.getJvmPartType());
    
    return Expressions.newArray(zero, zero);
  }

}
