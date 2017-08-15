/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.gcc.codegen.vptr;

import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.runtime.PointerImpls;
import org.renjin.repackaged.asm.Type;

/**
 * Defines the different kinds of types that a pointer can point to.
 */
public enum PointerType {


  BYTE(Type.BYTE_TYPE, PointerKind.INTEGRAL, Signedness.SIGNED, 1),
  SHORT(Type.SHORT_TYPE, PointerKind.INTEGRAL, Signedness.SIGNED, 2),
  CHAR(Type.CHAR_TYPE, PointerKind.INTEGRAL, Signedness.UNSIGNED, 2),
  INT(Type.INT_TYPE, PointerKind.INTEGRAL, Signedness.SIGNED, 4),
  LONG(Type.LONG_TYPE, PointerKind.INTEGRAL, Signedness.SIGNED, 8),
  FLOAT(Type.FLOAT_TYPE, PointerKind.FLOAT, Signedness.SIGNED, 4),
  DOUBLE(Type.DOUBLE_TYPE, PointerKind.FLOAT, Signedness.SIGNED, 8);

  private Type jvmType;
  private PointerKind kind;
  private int size;
  private Signedness signedness;

  PointerType(Type jvmType, PointerKind kind, Signedness signedness, int size) {
    this.jvmType = jvmType;
    this.kind = kind;
    this.size = size;
    this.signedness = signedness;
  }

  public Type getJvmType() {
    return jvmType;
  }

  public String titleCasedName() {
    return name().substring(0, 1) + name().substring(1).toLowerCase();
  }

  public String arrayBackedClassImpl() {
    return titleCasedName() + "ArrayPointer";
  }

  public Type arrayBackedImplType() {
    return Type.getType("L" + PointerImpls.PACKAGE.replace('.', '/') + "/" + arrayBackedClassImpl() + ";");
  }

  public static PointerType valueOf(GimplePrimitiveType primitiveType) {
    for (PointerType pointerType : values()) {
      if(pointerType.getJvmType().equals(primitiveType.jvmType())) {
        return pointerType;
      }
    }
    throw new IllegalArgumentException("type: " + primitiveType);
  }
}
