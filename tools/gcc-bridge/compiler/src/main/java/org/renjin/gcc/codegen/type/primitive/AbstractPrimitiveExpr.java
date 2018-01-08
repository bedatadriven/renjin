/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.expr.ConstantValue;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtrExpr;
import org.renjin.gcc.codegen.type.record.ProvidedPtrExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;

public abstract class AbstractPrimitiveExpr implements PrimitiveExpr {

  private final JExpr expr;
  private final PtrExpr address;

  protected AbstractPrimitiveExpr(JExpr expr, PtrExpr address) {
    this.expr = expr;
    this.address = address;
  }

  @Override
  public JExpr jexpr() {
    return expr;
  }



  @Override
  public PtrExpr addressOfReadOnly() {
    if(address != null) {
      return address;
    }
    // Otherwise create a temporary array which we can address
    PrimitiveType type = PrimitiveType.of(getType());
    JExpr tempArray = Expressions.newArray(type.jvmType(), Collections.singletonList(jexpr()));
    return new FatPtrPair(new PrimitiveValueFunction(type), tempArray);
  }

  @Override
  public final PtrExpr addressOf() {
    if(address == null) {
      throw new UnsupportedOperationException("Not addressable");
    }
    return address;
  }

  @Override
  public FunPtrExpr toFunPtr() throws UnsupportedCastException {
    if(ConstantValue.isZero(jexpr())) {
      return FunPtrExpr.NULL_PTR;
    }
    // Any non-zero constant won't be a valid function pointer..
    return FunPtrExpr.NULL_PTR;
  }

  @Override
  public FatArrayExpr toArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }

  @Override
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    return new VPtrExpr(Expressions.staticMethodCall(IntPtr.class, "toPtr",
        Type.getMethodDescriptor(Type.getType(Ptr.class), Type.INT_TYPE),
        toUnsignedInt(32).jexpr()));
  }

  @Override
  public ProvidedPtrExpr toProvidedPtrExpr(Type jvmType) {
    throw new UnsupportedCastException();
  }

  @Override
  public FatPtr toFatPtrExpr(ValueFunction valueFunction) {
    throw new UnsupportedCastException();
  }

  @Override
  public VPtrRecordExpr toVPtrRecord(GimpleRecordType recordType) {
    throw new UnsupportedCastException();
  }

  @Override
  public VArrayExpr toVArray(GimpleArrayType arrayType) {
    throw new UnsupportedCastException();
  }

  @Override
  public final PrimitiveExpr toPrimitiveExpr() throws UnsupportedCastException {
    return this;
  }

}
