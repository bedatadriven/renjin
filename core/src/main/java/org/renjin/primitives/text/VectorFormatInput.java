/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
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

package org.renjin.primitives.text;

import org.renjin.eval.EvalException;
import org.renjin.gcc.format.FormatInput;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.DoubleVector;

public class VectorFormatInput implements FormatInput {

  private final AtomicVector[] vectors;
  private final int[] vectorLengths;
  private int index;

  public VectorFormatInput(AtomicVector[] vectors) {
    this.vectors = vectors;
    this.vectorLengths = new int[vectors.length];
    for (int i = 0; i < vectors.length; i++) {
      vectorLengths[i] = vectors[i].length();
    }
  }

  public void next() {
    index++;
  }

  @Override
  public int getInt(int argumentIndex) {
    AtomicVector vector = vectors[argumentIndex];
    int index = elementIndex(argumentIndex);
    if(vector instanceof DoubleVector) {
      return (int)checkInt(vector.getElementAsDouble(index));
    } else {
      return vector.getElementAsInt(index);
    }
  }

  private int elementIndex(int argumentIndex) {
    return index % vectorLengths[argumentIndex];
  }

  @Override
  public long getLong(int argumentIndex) {
    AtomicVector vector = vectors[argumentIndex];
    int index = elementIndex(argumentIndex);

    if(vector instanceof DoubleVector) {
      return (long)checkInt(vector.getElementAsDouble(index));
    } else {
      return vector.getElementAsInt(index);
    }
  }

  @Override
  public long getUnsignedLong(int argumentIndex) {
    AtomicVector vector = vectors[argumentIndex];
    int index = elementIndex(argumentIndex);

    if(vector instanceof DoubleVector) {
      return (long)checkInt(vector.getElementAsDouble(index));
    } else {
      return Integer.toUnsignedLong(vector.getElementAsInt(index));
    }
  }

  @Override
  public double getDouble(int argumentIndex) {
    return vectors[argumentIndex].getElementAsDouble(elementIndex(argumentIndex));
  }

  @Override
  public String getString(int argumentIndex) {
    return vectors[argumentIndex].getElementAsString(elementIndex(argumentIndex));
  }


  private double checkInt(double x) {
    if(!Double.isFinite(x) || Math.floor(x) != x) {
      throw new EvalException("invalid integral format; use format %f, %e, %g or %a for numeric objects");
    }
    return x;
  }

}
