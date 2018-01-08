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
package org.renjin.gcc.gimple.type;

import org.renjin.gcc.gimple.expr.GimplePrimitiveConstant;
import org.renjin.repackaged.asm.Type;

public abstract class GimplePrimitiveType extends AbstractGimpleType {

  /**
   * @return the number of slots required to store this type on the stack or 
   * in the local variable table in the JVM.
   */
  public abstract int localVariableSlots();

  /**
   * 
   * @return the equivalent JVM type
   */
  public abstract Type jvmType();


  public static GimplePrimitiveType fromJvmType(Type type) {
    if(type.equals(Type.BOOLEAN_TYPE)) {
      return new GimpleBooleanType();

    } else if(type.equals(Type.DOUBLE_TYPE)) {
      return new GimpleRealType(64);

    } else if(type.equals(Type.FLOAT_TYPE)) {
      return new GimpleRealType(32);

    } else if(type.equals(Type.INT_TYPE)) {
      return new GimpleIntegerType(32);

    } else if(type.equals(Type.LONG_TYPE)) {
      return new GimpleIntegerType(64);

    } else if(type.equals(Type.CHAR_TYPE)) {
      return GimpleIntegerType.unsigned(16);

    } else if(type.equals(Type.SHORT_TYPE)) {
      return new GimpleIntegerType(16);

    } else if(type.equals(Type.BYTE_TYPE)) {
      return new GimpleIntegerType(8);

    } else {
      throw new UnsupportedOperationException("type: " + type);
    }
  }


  /**
   * @return constant expression of this type with the zero value.
   */
  public abstract GimplePrimitiveConstant zero();
}
