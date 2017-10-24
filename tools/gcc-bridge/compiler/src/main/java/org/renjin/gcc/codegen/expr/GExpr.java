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
package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.record.ProvidedPtrExpr;
import org.renjin.gcc.codegen.type.record.RecordArrayExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.repackaged.asm.Type;

/**
 * Interface for generators which can emit load/store operations for {@code GimpleExpr}s
 * 
 * <p>{@code Expr}s can either be simple, meaning they are represented by a single JVM value, or 
 * composite expressions, like {@link org.renjin.gcc.codegen.type.complex.ComplexValue} or 
 * {@link FatPtrPair} which are represented with multiple JVM values.</p>
 */
public interface GExpr {
  
  void store(MethodGenerator mv, GExpr rhs);
  
  GExpr addressOf();

  /**
   * Cast or transform this expression to a Function Pointer expression.
   */
  FunPtr toFunPtr() throws UnsupportedCastException;

  /**
   * Cast or transform this expression to an Array expression.
   */
  FatArrayExpr toArrayExpr() throws UnsupportedCastException;


  PrimitiveValue toPrimitiveExpr(GimplePrimitiveType targetType) throws UnsupportedCastException;

  VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException;

  /**
   * Cast or transform this expression to an Record Array expression.
   */
  RecordArrayExpr toRecordArrayExpr() throws UnsupportedCastException;

  /**
   * Cast or transform this expression to a Virtual Pointer expression.
   */
  VPtrExpr toVPtrExpr() throws UnsupportedCastException;

  /**
   * Cast or transform this expression to a record unit pointer.
   * @param jvmType
   */
  ProvidedPtrExpr toProvidedPtrExpr(Type jvmType);

  /**
   * Cast or transform this expression to a FatPtr
   * @param valueFunction
   */
  FatPtr toFatPtrExpr(ValueFunction valueFunction);

  VPtrRecordExpr toVPtrRecord(GimpleRecordType recordType);

  VArrayExpr toVArray(GimpleArrayType arrayType);

  NumericExpr toNumericExpr();
}
