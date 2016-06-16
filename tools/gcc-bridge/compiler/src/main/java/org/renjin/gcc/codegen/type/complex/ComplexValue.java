package org.renjin.gcc.codegen.type.complex;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.primitive.op.NegativeValue;


/**
 * Complex numerical value
 */
public class ComplexValue implements GExpr {
  private FatPtrExpr address;
  private JExpr realValue;
  private JExpr imaginaryValue;
  private Type componentType;
  
  public ComplexValue(FatPtrExpr address, JExpr realValue, JExpr imaginaryValue) {
    this.address = address;
    this.realValue = realValue;
    this.imaginaryValue = imaginaryValue;
    
    if(!realValue.getType().equals(imaginaryValue.getType())) {
      throw new IllegalArgumentException(String.format("Part types do not match: %s != %s", 
          realValue.getType(), imaginaryValue.getType()));
    }
    this.componentType = realValue.getType();
  }

  public ComplexValue(JExpr realValue, JExpr imaginaryValue) {
    this(null, realValue, imaginaryValue);
  }
  
  public ComplexValue(GExpr realValue, GExpr imaginaryValue) {
    this(null, ((PrimitiveValue) realValue).unwrap(), ((PrimitiveValue) imaginaryValue).unwrap());
  }

  public ComplexValue(JExpr realValue) {
    this.realValue = realValue;
    this.imaginaryValue = Expressions.zero(realValue.getType());
  }

  public Type getComponentType() {
    return componentType;
  }

  public JExpr getRealJExpr() {
    return realValue;
  }
  
  public GExpr getRealGExpr() {
    return new PrimitiveValue(realValue);
  }

  public JExpr getImaginaryJExpr() {
    return imaginaryValue;
  }
  
  public GExpr getImaginaryGExpr() {
    return new PrimitiveValue(imaginaryValue);
  }
  
  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    
    ComplexValue complexRhs = (ComplexValue) rhs;
    
    ((JLValue) realValue).store(mv, complexRhs.getRealJExpr());
    ((JLValue) imaginaryValue).store(mv, complexRhs.getImaginaryJExpr());
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
