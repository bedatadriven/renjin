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

import org.renjin.gcc.codegen.vptr.PointerType;
import org.renjin.repackaged.asm.Type;

import java.io.PrintWriter;

/*
 * Generates array-backed implementations of Pointer implementations
 */
public class PointerImpls {

  public static final String PACKAGE = "org.renjin.gcc.runtime";

  public static PointerType ofType(Type type) {
    switch (type.getSort()) {
      case Type.BYTE:
        return PointerType.BYTE;
      case Type.SHORT:
        return PointerType.SHORT;
      case Type.CHAR:
        return PointerType.CHAR;
      case Type.INT:
        return PointerType.INT;
      case Type.LONG:
        return PointerType.LONG;
      case Type.FLOAT:
        return PointerType.FLOAT;
      case Type.DOUBLE:
        return PointerType.DOUBLE;
    }
    throw new UnsupportedOperationException("TODO: " + type);
  }

  public static void main(String[] args) {

//    for (Type type : Type.values()) {
//      writeImpl(type);
//    }
//

    writeImpl(PointerType.INT);

  }

  private static void writeImpl(PointerType primitiveType) {

    PrintWriter pw = new PrintWriter(System.out);
    String className = primitiveType.titleCasedName() + "ArrayPointer";

    pw.println("package org.renjin.gcc.runtime;");
    pw.println();


  }

}
