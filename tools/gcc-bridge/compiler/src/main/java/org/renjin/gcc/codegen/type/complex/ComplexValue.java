package org.renjin.gcc.codegen.type.complex;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.type.primitive.op.NegativeValue;


/**
 * Complex numerical value
 */
public class ComplexValue implements Expr, LValue<ComplexValue>, Addressable {
  private FatPtrExpr address;
  private SimpleExpr realValue;
  private SimpleExpr imaginaryValue;
  private Type componentType;
  
  public ComplexValue(FatPtrExpr address, SimpleExpr realValue, SimpleExpr imaginaryValue) {
    this.address = address;
    this.realValue = realValue;
    this.imaginaryValue = imaginaryValue;
    
    if(!realValue.getType().equals(imaginaryValue.getType())) {
      throw new IllegalArgumentException(String.format("Part types do not match: %s != %s", 
          realValue.getType(), imaginaryValue.getType()));
    }
    this.componentType = realValue.getType();
  }

  public ComplexValue(SimpleExpr realValue, SimpleExpr imaginaryValue) {
    this(null, realValue, imaginaryValue);
  }

  public ComplexValue(SimpleExpr realValue) {
    this.realValue = realValue;
    this.imaginaryValue = Expressions.zero(realValue.getType());
  }

  public Type getComponentType() {
    return componentType;
  }

  public SimpleExpr getRealValue() {
    return realValue;
  }

  public SimpleExpr getImaginaryValue() {
    return imaginaryValue;
  }
  
  @Override
  public void store(MethodGenerator mv, ComplexValue complexValue) {
    ((SimpleLValue) realValue).store(mv, complexValue.getRealValue());
    ((SimpleLValue) imaginaryValue).store(mv, complexValue.getImaginaryValue());
  }

  /**
   * Generates the complex conjugate of a complex number 
   *
   * <p>The conjugate is the number with equal real part and imaginary part equal in magnitude but opposite in sign. 
   * For example, the complex conjugate of 3 + 4i is 3 − 4i.
   */
  public ComplexValue conjugate() {
    return new ComplexValue(address, realValue, new NegativeValue(imaginaryValue));
  }

  @Override
  public FatPtrExpr addressOf() {
    if(address == null) {
      throw new UnsupportedOperationException("not addressable");
    }
    return address;
  }
}
