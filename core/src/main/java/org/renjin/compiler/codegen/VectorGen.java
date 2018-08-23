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
package org.renjin.compiler.codegen;

import org.renjin.compiler.ir.TypeSet;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.LogicalArrayVector;

public enum VectorGen {

  LOGICAL {
    @Override
    public Type getElementType() {
      return Type.INT_TYPE;
    }

    @Override
    public Type getVectorArrayType() {
      return Type.getType(LogicalArrayVector.class);
    }
  },
  INTEGER {
    @Override
    public Type getElementType() {
      return Type.INT_TYPE;
    }

    @Override
    public Type getVectorArrayType() {
      return Type.getType(IntArrayVector.class);
    }
  },
  DOUBLE {
    @Override
    public Type getElementType() {
      return Type.DOUBLE_TYPE;
    }

    @Override
    public Type getVectorArrayType() {
      return Type.getType(DoubleArrayVector.class);
    }
  };

  public abstract Type getElementType();

  /**
   *
   * @return the type of this class's Array implementation, like DoubleArrayVector, IntArrayVector, etc.
   */
  public abstract Type getVectorArrayType();


  public static VectorGen forType(int typeSet) {
    switch (typeSet) {
      case TypeSet.LOGICAL:
        return LOGICAL;
      case TypeSet.INT:
        return INTEGER;
      case TypeSet.DOUBLE:
        return DOUBLE;
    }
    throw new IllegalArgumentException("typeSet: " + TypeSet.toString(typeSet));
  }

  public Type getArrayType() {
    return Type.getType("[" + getElementType().getDescriptor());

  }
}
