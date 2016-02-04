package org.renjin.gcc.codegen.type.complex;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.op.NegateGenerator;
import org.renjin.gcc.codegen.var.LValue;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;
import org.renjin.gcc.codegen.var.Var;


/**
 * Complex numerical value
 */
public class ComplexValue implements ExprGenerator, LValue<ComplexValue> {
  private Value realValue;
  private Value imaginaryValue;
  private Type componentType;

  public ComplexValue(Value realValue, Value imaginaryValue) {
    this.realValue = realValue;
    this.imaginaryValue = imaginaryValue;
    
    if(!realValue.getType().equals(imaginaryValue.getType())) {
      throw new IllegalArgumentException(String.format("Part types do not match: %s != %s", 
          realValue.getType(), imaginaryValue.getType()));
    }
    this.componentType = realValue.getType();
  }

  public ComplexValue(Value realValue) {
    this.realValue = realValue;
    this.imaginaryValue = Values.zero(realValue.getType());
  }

  public Type getComponentType() {
    return componentType;
  }

  public Value getRealValue() {
    return realValue;
  }

  public Value getImaginaryValue() {
    return imaginaryValue;
  }
  
  @Override
  public void store(MethodGenerator mv, ComplexValue complexValue) {
    ((Var) realValue).store(mv, complexValue.getRealValue());
    ((Var) imaginaryValue).store(mv, complexValue.getImaginaryValue());
  }

  /**
   * Generates the complex conjugate of a complex number 
   *
   * <p>The conjugate is the number with equal real part and imaginary part equal in magnitude but opposite in sign. 
   * For example, the complex conjugate of 3 + 4i is 3 − 4i.
   */
  public ComplexValue conjugate() {
    return new ComplexValue(realValue, new NegateGenerator(imaginaryValue));
  }

}
