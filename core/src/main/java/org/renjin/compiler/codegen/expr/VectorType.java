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
package org.renjin.compiler.codegen.expr;

import org.renjin.compiler.ir.TypeSet;
import org.renjin.repackaged.asm.ByteVector;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.StringVector;

public enum VectorType {
  BYTE(Type.BYTE_TYPE, Type.getType(ByteVector.class)),
  LOGICAL(Type.INT_TYPE, Type.getType(LogicalVector.class)),
  INT(Type.INT_TYPE, Type.getType(IntVector.class)),
  DOUBLE(Type.DOUBLE_TYPE, Type.getType(DoubleVector.class)),
  STRING(Type.getType(String.class), Type.getType(StringVector.class));

  Type jvmType;
  Type vectorClassType;

  private VectorType(Type jvmType, Type vectorClassType) {
    this.jvmType = jvmType;
    this.vectorClassType = vectorClassType;
  }

  public Type getJvmType() {
    return jvmType;
  }

  public Type getJvmArrayType() {
    return Type.getType("[" + getJvmType().getDescriptor());
  }

  public Type getVectorClassType() {
    return vectorClassType;
  }

  public static VectorType of(int typeSet) {
    assert TypeSet.size(typeSet) == 1;
    switch (typeSet) {
      case TypeSet.RAW:
        return VectorType.BYTE;
      case TypeSet.LOGICAL:
        return VectorType.LOGICAL;
      case TypeSet.INT:
        return VectorType.INT;
      case TypeSet.DOUBLE:
        return VectorType.DOUBLE;
      case TypeSet.STRING:
        return VectorType.STRING;
      default:
        throw new UnsupportedOperationException(TypeSet.toString(typeSet));
    }
  }

  public static VectorType fromJvmType(Class type) {
    if(type.equals(byte.class)) {
      return BYTE;
    } else if(type.equals(int.class)) {
      return INT;
    } else if(type.equals(double.class)) {
      return DOUBLE;
    } else if(type.equals(String.class)) {
      return STRING;
    } else if(type.equals(boolean.class)) {
      return LOGICAL;
    } else {
      throw new IllegalArgumentException(type.getName());
    }
  }
}
