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
package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.ClassVisitor;

/**
 * Generates field definitions, loads and stores for global variables
 */
public abstract class FieldStrategy {

  public void emitInstanceInit(MethodGenerator mv) {
  }

  public abstract void writeFields(ClassVisitor cv);


  /**
   * Returns a {@code {@link GExpr} for this field of the given {@code instance}.
   * 
   * @param instance the instance of the class from which to access the member
   * @param offset the offset, in bits, from the <em>start</em> of this field.
   * @param size The size of the value, in bits, to access.
   * @param expectedType The strategy for the expected type of the field.
   */
  public abstract GExpr memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType);
  
  public abstract void copy(MethodGenerator mv, JExpr source, JExpr dest);
  
  public abstract void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount);

}
