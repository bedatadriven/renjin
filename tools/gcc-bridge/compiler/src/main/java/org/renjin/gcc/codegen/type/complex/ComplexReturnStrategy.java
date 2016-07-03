package org.renjin.gcc.codegen.type.complex;


import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.repackaged.asm.Type;

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
  public JExpr marshall(GExpr expr) {
    ComplexValue complexValue = (ComplexValue) expr;
    return Expressions.newArray(
        complexValue.getRealJExpr(),
        complexValue.getImaginaryJExpr());
  }

  @Override
  public GExpr unmarshall(MethodGenerator mv, JExpr returnValue, TypeStrategy lhsTypeStrategy) {
    // Allocate a temporary variable for the array so that it's 
    // components can be accessed
    JLValue array = mv.getLocalVarAllocator().reserve("retval", returnValue.getType());
    array.store(mv, returnValue);
    JExpr realValue = Expressions.elementAt(array, 0);
    JExpr imaginaryValue = Expressions.elementAt(array, 1);
    
    return new ComplexValue(realValue, imaginaryValue);
  }

  @Override
  public JExpr getDefaultReturnValue() {
    JExpr zero = Expressions.zero(type.getJvmPartType());
    
    return Expressions.newArray(zero, zero);
  }

}
