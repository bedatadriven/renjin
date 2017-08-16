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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayExpr;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.GSimpleExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.op.CastGenerator;
import org.renjin.gcc.codegen.type.record.RecordArrayExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;


public class PrimitiveValue implements GSimpleExpr {

  private GimplePrimitiveType primitiveType;
  private JExpr expr;
  private GExpr address;

  public PrimitiveValue(JExpr expr) {
    this.expr = expr;
    this.primitiveType = GimplePrimitiveType.fromJvmType(expr.getType());
  }

  public PrimitiveValue(GimplePrimitiveType primitiveType, JExpr expr) {
    this.primitiveType = primitiveType;
    this.expr = expr;
  }

  public PrimitiveValue(JExpr expr, GExpr address) {
    this.expr = expr;
    this.primitiveType = GimplePrimitiveType.fromJvmType(expr.getType());
    this.address = address;
  }

  public PrimitiveValue(GimplePrimitiveType primitiveType, JExpr expr, GExpr address) {
    this.primitiveType = primitiveType;
    this.expr = expr;
    this.address = address;
  }

  public JExpr getExpr() {
    return expr;
  }

  @Override
  public GExpr addressOf() {
    if(address == null) {
      throw new UnsupportedOperationException("Not addressable");
    }
    return address;
  }

  @Override
  public FunPtr toFunPtr() {
    return FunPtr.NULL_PTR;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    
    PrimitiveValue primitiveRhs = (PrimitiveValue) rhs;
    
    ((JLValue) expr).store(mv, primitiveRhs.getExpr());
  }

  @Override
  public JExpr unwrap() {
    return expr;
  }

  @Override
  public ArrayExpr toArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }

  @Override
  public PrimitiveValue toPrimitiveExpr(GimplePrimitiveType targetType) throws UnsupportedCastException {
    return new PrimitiveValue(new CastGenerator(unwrap(), primitiveType, targetType));
  }

  @Override
  public VoidPtr toVoidPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public RecordArrayExpr toRecordArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }
}
