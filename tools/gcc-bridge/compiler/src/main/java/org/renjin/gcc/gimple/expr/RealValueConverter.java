/**
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
package org.renjin.gcc.gimple.expr;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

/**
 * Deserializes real values from Json, including "Inf", "-Inf"
 */
public class RealValueConverter implements Converter<Object, Double> {
  @Override
  public Double convert(Object value) {
    if(value instanceof Number) {
      return ((Number) value).doubleValue();
    } else if(value instanceof String) {
      return parseString(((String) value));
    } else {
      throw new RuntimeException("invalid value: " + value);
    }
  }

  @Override
  public JavaType getInputType(TypeFactory typeFactory) {
    return typeFactory.constructType(String.class);
  }

  @Override
  public JavaType getOutputType(TypeFactory typeFactory) {
    return typeFactory.constructType(Double.class);
  }

  private double parseString(String stringValue) {
    if(stringValue.equals("Inf")) {
      return Double.POSITIVE_INFINITY;
    } else if(stringValue.equals("-Inf")) {
      return Double.NEGATIVE_INFINITY;
    } else {
      return Double.parseDouble(stringValue);
    }
  }
}
