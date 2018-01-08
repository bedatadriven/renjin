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
package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.util.List;

/**
 * Functor which can "unwrap" a fat ptr
 */
public interface ValueFunction {
  
  Type getValueType();

  GimpleType getGimpleValueType();

  /**
   * Returns the number of array elements required for each value.
   * 
   * <p>For an array of doubles, for example, the length is 1. By contrast, a complex number value requires
   * two array elements per value.</p>
   */
  int getElementLength();

  /**
   * Returns the size of each array value in bytes. This value is used to convert
   * offsets in bytes to offsets in number of elements. 
   */
  int getArrayElementBytes();

  Optional<JExpr> getValueConstructor();

  VPtrExpr toVPtr(JExpr array, JExpr offset);

  GExpr dereference(JExpr array, JExpr offset);
  
  GExpr dereference(WrappedFatPtrExpr wrapperInstance);

  /**
   * Transforms the given expression to one or more array element values.
   * @param expr
   * @return
   */
  List<JExpr> toArrayValues(GExpr expr);


  /**
   * Copies the <strong>values</strong> from one array of these values to another.
   * 
   * @param valueCount The number of <strong>values</strong> to copy. Keep in mind that one
   *                   value may require several array elements. For example, a complex value occupies
   *                   two array elements.
   */
  void memoryCopy(MethodGenerator mv, 
                  JExpr destinationArray, JExpr destinationOffset, 
                  JExpr sourceArray, JExpr sourceOffset, JExpr valueCount);
  
  
  void memorySet(MethodGenerator mv, JExpr array, JExpr offset, JExpr byteValue, JExpr length);
  
}

