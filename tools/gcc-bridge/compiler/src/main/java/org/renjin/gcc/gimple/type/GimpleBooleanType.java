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
package org.renjin.gcc.gimple.type;

import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.expr.GimplePrimitiveConstant;
import org.renjin.repackaged.asm.Type;

public class GimpleBooleanType extends GimplePrimitiveType {

  public GimpleBooleanType() {
    setSize(8);
  }

  @Override
  public String toString() {
    return "bool";
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof GimpleBooleanType;
  }

  @Override
  public int hashCode() {
    return 1;
  }


  @Override
  public int localVariableSlots() {
    return 1;
  }

  @Override
  public Type jvmType() {
    return Type.BOOLEAN_TYPE;
  }

  @Override
  public int sizeOf() {
    return 1;
  }

  @Override
  public GimplePrimitiveConstant zero() {
    return new GimpleIntegerConstant(this, false);
  }
}
