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

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;

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
  public void store(MethodGenerator mv, GExpr rhs) {
    RecordArrayExpr arrayRhs = (RecordArrayExpr) rhs;
    mv.arrayCopy(arrayRhs.getArray(), arrayRhs.getOffset(), array, offset, constantInt(arrayLength));
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
