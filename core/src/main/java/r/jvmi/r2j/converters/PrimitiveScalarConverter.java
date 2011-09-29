package r.jvmi.r2j.converters;

import r.lang.AtomicVector;
import r.lang.SEXP;
import r.lang.Vector;
import r.lang.exception.EvalException;

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
