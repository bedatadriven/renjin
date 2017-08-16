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
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayExpr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.gcc.runtime.Record;

import static org.renjin.gcc.codegen.expr.Expressions.*;

/**
 * Record value expression, backed by a JVM primitive array 
 */
public final class RecordArrayExpr implements GExpr {


  private RecordArrayValueFunction valueFunction;
  private JExpr array;
  private JExpr offset;
  private int arrayLength;

  public RecordArrayExpr(RecordArrayValueFunction valueFunction, JExpr array, JExpr offset, int arrayLength) {
    this.valueFunction = valueFunction;
    this.array = array;
    this.offset = offset;
    this.arrayLength = arrayLength;
  }

  public RecordArrayExpr(RecordArrayValueFunction valueFunction, JExpr array, int arrayLength) {
    this(valueFunction, array, zero(), arrayLength);
  }
  
  @Override
  public GExpr addressOf() {
    return new FatPtrPair(valueFunction, array, offset);
  }

  @Override
  public FunPtr toFunPtr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ArrayExpr toArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PrimitiveValue toPrimitiveExpr(GimplePrimitiveType targetType) throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VoidPtr toVoidPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public RecordArrayExpr toRecordArrayExpr() throws UnsupportedCastException {
    return this;
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    // GCC Return Value Optimization sometimes omits the valueOf operator in the gimple output
    // handle the case of a pointer being passed
    if(rhs instanceof FatPtrPair) {
      FatPtrPair fatPtrExpr = (FatPtrPair) rhs;
      mv.arrayCopy(fatPtrExpr.getArray(), fatPtrExpr.getOffset(), array, offset, constantInt(arrayLength));
    } else if (rhs instanceof RecordArrayExpr) {
      RecordArrayExpr arrayRhs = (RecordArrayExpr) rhs;
      mv.arrayCopy(arrayRhs.getArray(), arrayRhs.getOffset(), array, offset, constantInt(arrayLength));
    } else {
      throw new InternalCompilerException("Cannot assign " + rhs + " to " + this);
    }
  }

  public JExpr getArray() {
    return array;
  }

  public JExpr getOffset() {
    return offset;
  }

  public JExpr copyArray() {
    return copyOfArrayRange(array, offset, sum(offset, arrayLength));
  }
}
