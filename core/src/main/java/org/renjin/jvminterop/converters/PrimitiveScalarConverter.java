package org.renjin.jvminterop.converters;

import org.renjin.eval.EvalException;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


public abstract class PrimitiveScalarConverter<T> implements Converter<T> {

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
