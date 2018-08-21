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
package org.renjin.gcc.format;

import org.renjin.gcc.runtime.Ptr;
import org.renjin.gcc.runtime.Stdlib;

public class FormatArrayInput implements FormatInput {

  private final Object[] arguments;

  public FormatArrayInput(Object[] arguments) {
    this.arguments = arguments;
  }

  @Override
  public int getInt(int argumentIndex) {
    Object value = arguments[argumentIndex];
    if(value instanceof Number) {
      return ((Number) value).intValue();
    } else if(value instanceof Character) {
      return (int) (Character) value;
    } else if(value instanceof Ptr) {
      return ((Ptr) value).toInt();
    } else {
      throw new IllegalArgumentException("Expected integer argument at index " + argumentIndex + ", found " +
          value.getClass().getName());
    }
  }

  @Override
  public long getLong(int argumentIndex) {
    return ((Number)arguments[argumentIndex]).longValue();
  }

  @Override
  public long getUnsignedLong(int argumentIndex) {
    Object value = arguments[argumentIndex];
    if(value instanceof Long) {
      return (Long) value;
    } else if(value instanceof Integer) {
      return Integer.toUnsignedLong((int)value);

    } else {
      throw new IllegalArgumentException("Expected Integer or Long argument at index " + argumentIndex + ", found " +
          value.getClass().getName());
    }
  }

  @Override
  public double getDouble(int argumentIndex) {
    return (Double)arguments[argumentIndex];
  }

  @Override
  public String getString(int argumentIndex) {
    Object value = arguments[argumentIndex];
    if(value instanceof String) {
      return (String) value;
    } else if(value instanceof Ptr) {
      return Stdlib.nullTerminatedString((Ptr) value);
    } else {
      throw new IllegalArgumentException("Expected string argument at index " + argumentIndex);
    }
  }
}
