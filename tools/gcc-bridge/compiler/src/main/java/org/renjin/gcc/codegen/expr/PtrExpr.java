/*
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
package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Label;

/**
 * Marker interface for pointer expressions
 */
public interface PtrExpr extends GExpr {
  
  void jumpIfNull(MethodGenerator mv, Label label);

  JExpr memoryCompare(MethodGenerator mv, PtrExpr otherPointer, JExpr n);

  void memorySet(MethodGenerator mv, JExpr byteValue, JExpr length);

  /**
   * Copies memory from the given {@code source} {@code PtrExpr} to memory to which
   * this pointer points.
   *
   * @param source the pointer to the memory to copy
   * @param length the number of bytes to copy
   * @param buffer
   */
  void memoryCopy(MethodGenerator mv, PtrExpr source, JExpr length, boolean buffer);

  PtrExpr realloc(MethodGenerator mv, JExpr newSizeInBytes);

  PtrExpr pointerPlus(MethodGenerator mv, JExpr offsetInBytes);

  GExpr valueOf(GimpleType expectedType);

  ConditionGenerator comparePointer(MethodGenerator mv, GimpleOp op, GExpr otherPointer);

}
