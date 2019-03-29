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

package org.renjin.sexp;

import java.util.Arrays;

public final class CompiledFunctionEnvironment extends Environment {

  private final SEXP[] frameArray;

  public static CompiledFunctionEnvironment init(Environment parent, SEXP[] arguments, SEXP variableNames) {
    return new CompiledFunctionEnvironment(parent, ((ListVector) variableNames).toArrayUnsafe(), arguments);
  }

  private CompiledFunctionEnvironment(Environment parent, SEXP[] variableNames, SEXP[] arguments) {
    super(AttributeMap.EMPTY);
    this.frameArray = Arrays.copyOf(arguments, variableNames.length);
    this.frame = new CompiledFrame(variableNames, arguments, frameArray);
    this.setParent(parent);
  }

  public void set(int index, SEXP value) {
    frameArray[index] = value;
  }

  public SEXP get(int index) {
    SEXP value = frameArray[index];
    if(value != null) {
      return value;
    }
    throw new UnsupportedOperationException("TODO");
  }



  public Promise promise(int index) {
    throw new UnsupportedOperationException("TODO");
  }
}
