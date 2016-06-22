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
