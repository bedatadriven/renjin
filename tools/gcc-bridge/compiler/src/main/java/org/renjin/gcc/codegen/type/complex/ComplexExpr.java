/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen.type.complex;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtrExpr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveExpr;
import org.renjin.gcc.codegen.type.primitive.RealExpr;
import org.renjin.gcc.codegen.type.record.ProvidedPtrExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.repackaged.asm.Type;

import static org.renjin.gcc.codegen.expr.Expressions.difference;
import static org.renjin.gcc.codegen.expr.Expressions.product;
import static org.renjin.gcc.codegen.expr.Expressions.sum;


/**
 * Complex numerical value
 */
public class ComplexExpr implements NumericExpr {
  private PtrExpr address;
  private JExpr realValue;
  private JExpr imaginaryValue;
  private Type componentType;
  
  public ComplexExpr(PtrExpr address, JExpr realValue, JExpr imaginaryValue) {
    this.address = address;
    this.realValue = realValue;
    this.imaginaryValue = imaginaryValue;
    
    if(!realValue.getType().equals(imaginaryValue.getType())) {
      throw new IllegalArgumentException(String.format("Part types do not match: %s != %s", 
          realValue.getType(), imaginaryValue.getType()));
    }
    this.componentType = realValue.getType();
  }

  public GimpleRealType getGimpleComponentType() {
    return ((GimpleRealType) GimplePrimitiveType.fromJvmType(componentType));
  }

  public ComplexExpr(JExpr realValue, JExpr imaginaryValue) {
    this(null, realValue, imaginaryValue);
  }
  
  public ComplexExpr(GExpr realValue, GExpr imaginaryValue) {
    this(null, ((PrimitiveExpr) realValue).jexpr(), ((PrimitiveExpr) imaginaryValue).jexpr());
  }

  public ComplexExpr(JExpr realValue) {
    this.realValue = realValue;
    this.imaginaryValue = Expressions.zero(realValue.getType());
  }

  public Type getComponentType() {
    return componentType;
  }

  public JExpr getRealJExpr() {
    return realValue;
  }
  
  public RealExpr getRealGExpr() {
    return new RealExpr(getGimpleComponentType(), realValue);
  }

  public JExpr getImaginaryJExpr() {
    return imaginaryValue;
  }
  
  public RealExpr getImaginaryGExpr() {
    return new RealExpr(getGimpleComponentType(), imaginaryValue);
  }
  
  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    
    ComplexExpr complexRhs = (ComplexExpr) rhs;
    
    ((JLValue) realValue).store(mv, complexRhs.getRealJExpr());
    ((JLValue) imaginaryValue).store(mv, complexRhs.getImaginaryJExpr());
  }

  /**
   * Generates the complex conjugate of a complex number 
   *
   * <p>The conjugate is the number with equal real part and imaginary part equal in magnitude but opposite in sign. 
   * For example, the complex conjugate of 3 + 4i is 3 − 4i.
   */
  public ComplexExpr conjugate() {
    return new ComplexExpr(address, realValue, Expressions.negative(imaginaryValue));
  }

  @Override
  public PtrExpr addressOf() {
    if(address == null) {
      throw new UnsupportedOperationException("not addressable");
    }
    return address;
  }

  @Override
  public FunPtrExpr toFunPtr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FatArrayExpr toArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PrimitiveExpr toPrimitiveExpr() throws UnsupportedCastException {
    return getRealGExpr().toPrimitiveExpr();
  }

  @Override
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ProvidedPtrExpr toProvidedPtrExpr(Type jvmType) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FatPtr toFatPtrExpr(ValueFunction valueFunction) {
    throw new UnsupportedCastException();
  }

  @Override
  public VPtrRecordExpr toVPtrRecord(GimpleRecordType recordType) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VArrayExpr toVArray(GimpleArrayType arrayType) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public NumericExpr toNumericExpr() {
    return this;
  }

  @Override
  public NumericExpr plus(GExpr operand) {
    JExpr real = sum(realValue, operand.toNumericExpr().toComplexExpr().getRealJExpr());
    JExpr im = sum(imaginaryValue, operand.toNumericExpr().toComplexExpr().getImaginaryJExpr());
    return new ComplexExpr(real, im);
  }

  @Override
  public NumericExpr minus(GExpr operand) {
    JExpr real = difference(realValue, operand.toNumericExpr().toComplexExpr().getRealJExpr());
    JExpr im = difference(imaginaryValue, operand.toNumericExpr().toComplexExpr().getImaginaryJExpr());
    return new ComplexExpr(real, im);
  }

  @Override
  public NumericExpr multiply(GExpr operand) {
    //(a + bi)(c + di) = (ac - bd) + (bc + ad)i
    ComplexExpr x = this;
    ComplexExpr y = operand.toNumericExpr().toComplexExpr();

    JExpr a = x.realValue;
    JExpr b = x.imaginaryValue;
    JExpr c = y.realValue;
    JExpr d = y.imaginaryValue;

    JExpr real = difference(product(a, c), product(b, d));
    JExpr im = sum(product(b, c), product(a, d));

    return new ComplexExpr(real, im);
  }

  @Override
  public NumericExpr divide(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public NumericExpr negative() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public NumericExpr min(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public NumericExpr max(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public NumericExpr absoluteValue() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ComplexExpr toComplexExpr() {
    return this;
  }
}
