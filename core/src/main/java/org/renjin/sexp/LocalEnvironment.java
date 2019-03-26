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

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;

import java.util.Arrays;

public final class LocalEnvironment extends Environment {

  private final SEXP[] frameArray;

  public static LocalEnvironment init(Environment parent, SEXP[] arguments, int frameSize) {
    return new LocalEnvironment(parent, Arrays.copyOf(arguments, frameSize));
  }

  public LocalEnvironment(Environment parent, SEXP[] frameArray) {
    super(AttributeMap.EMPTY);
    this.frameArray = frameArray;
    this.frame = new LocalFrame(frameArray);
    this.setParent(parent);
  }

  public void set(int index, SEXP value) {
    frameArray[index] = value;
  }

  public SEXP get(int index) {
    return frameArray[index];
  }

  public Promise promise(int index) {
    throw new UnsupportedOperationException("TODO");
  }

  public Function findFunctionOrThrow(Context context, String functionName) {
    Function function = findFunction(context, Symbol.get(functionName));
    if(function == null) {
      throw new EvalException("Could not find function \"%s\"", functionName);
    }
    return function;
  }

}
