/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.invoke.reflection.converters;

import org.renjin.eval.EvalException;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


/**
 * Base class for converters between JVM scalars and R {@code vector}s
 * @param <T>
 */
public abstract class PrimitiveScalarConverter<T> implements Converter<T>, AtomicVectorConverter {

  @Override
  public final Object convertToJava(SEXP value) {
    if(!(value instanceof AtomicVector)) {
      throw new EvalException("Cannot convert '%s' to boolean", value.getTypeName());
    } else if(value.length() < 1) {
      throw new EvalException("Cannot pass empty vector to boolean argument");
    }
    return getFirstElement((Vector)value);
  }
  
  protected abstract Object getFirstElement(Vector value);

}
