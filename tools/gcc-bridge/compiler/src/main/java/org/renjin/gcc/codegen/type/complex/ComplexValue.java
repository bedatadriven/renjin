package org.renjin.gcc.codegen.type.complex;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Var;


/**
 * Complex numerical value
 */
public class ComplexValue implements ExprGenerator {
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

  public Type getComponentType() {
    return componentType;
  }

  public Value getRealValue() {
    return realValue;
  }

  public Value getImaginaryValue() {
    return imaginaryValue;
  }
  
  public void store(MethodGenerator mv, ComplexValue complexValue) {
    ((Var) realValue).store(mv, complexValue.getRealValue());
    ((Var) imaginaryValue).store(mv, complexValue.getImaginaryValue());
  }
}
