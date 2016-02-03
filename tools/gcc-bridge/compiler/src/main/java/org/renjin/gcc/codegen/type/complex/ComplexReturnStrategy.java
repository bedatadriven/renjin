package org.renjin.gcc.codegen.type.complex;


import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;
import org.renjin.gcc.codegen.var.Var;
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
  public Value marshall(ExprGenerator expr) {
    ComplexValue complexValue = (ComplexValue) expr;
    return Values.newArray(
        complexValue.getRealValue(), 
        complexValue.getImaginaryValue());
  }

  @Override
  public ExprGenerator unmarshall(MethodGenerator mv, Value returnValue) {
    // Allocate a temporary variable for the array so that it's 
    // components can be accessed
    Var array = mv.getLocalVarAllocator().reserve("retval", returnValue.getType());
    array.store(mv, returnValue);
    Value realValue = Values.elementAt(array, 0);
    Value imaginaryValue = Values.elementAt(array, 1);
    
    return new ComplexValue(realValue, imaginaryValue);
  }

}
