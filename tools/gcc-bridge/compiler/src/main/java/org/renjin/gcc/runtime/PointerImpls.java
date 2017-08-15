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

package org.renjin.gcc.runtime;

import org.renjin.repackaged.asm.Type;

import java.io.PrintWriter;

/*
 * Generates array-backed implementations of Pointer implementations
 */
public class PointerImpls {

  private static final String PACKAGE = "org.renjin.gcc.runtime";

  public enum Signedness {
    SIGNED,
    UNSIGNED
  }

  public enum Kind {
    INTEGRAL,
    FLOAT
  }

  public enum PrimitiveType {
    BYTE(Kind.INTEGRAL, Signedness.SIGNED, 1),
    SHORT(Kind.INTEGRAL, Signedness.SIGNED, 2),
    CHAR(Kind.INTEGRAL, Signedness.UNSIGNED, 2),
    INT(Kind.INTEGRAL, Signedness.SIGNED, 4),
    LONG(Kind.INTEGRAL, Signedness.SIGNED, 8),
    FLOAT(Kind.FLOAT, Signedness.SIGNED, 4),
    DOUBLE(Kind.FLOAT, Signedness.SIGNED, 8);

    private Kind kind;
    private int size;
    private Signedness signedness;

    PrimitiveType(Kind kind, Signedness signedness, int size) {
      this.kind = kind;
      this.size = size;
      this.signedness = signedness;
    }

    public String titleCasedName() {
      return name().substring(0, 1) + name().substring(1).toLowerCase();
    }

    public String arrayBackedClassImpl() {
      return titleCasedName() + "ArrayPointer";
    }

    public Type arrayBackedImplType() {
      return Type.getType("L" + PACKAGE.replace('.', '/') + "/" + arrayBackedClassImpl() + ";");
    }
  }

  public static PrimitiveType ofType(Type type) {
    switch (type.getSort()) {
      case Type.BYTE:
        return PrimitiveType.BYTE;
      case Type.SHORT:
        return PrimitiveType.SHORT;
      case Type.CHAR:
        return PrimitiveType.CHAR;
      case Type.INT:
        return PrimitiveType.INT;
      case Type.LONG:
        return PrimitiveType.LONG;
      case Type.FLOAT:
        return PrimitiveType.FLOAT;
      case Type.DOUBLE:
        return PrimitiveType.DOUBLE;
    }
    throw new UnsupportedOperationException("TODO: " + type);
  }

  public static void main(String[] args) {

//    for (Type type : Type.values()) {
//      writeImpl(type);
//    }
//

    writeImpl(PrimitiveType.INT);

  }

  private static void writeImpl(PrimitiveType primitiveType) {

    PrintWriter pw = new PrintWriter(System.out);
    String className = primitiveType.titleCasedName() + "ArrayPointer";

    pw.println("package org.renjin.gcc.runtime;");
    pw.println();


  }

}
