package r.jvmi.r2j.converters;

import r.lang.AtomicVector;
import r.lang.Null;
import r.lang.SEXP;
import r.lang.Vector;
import r.lang.exception.EvalException;

public abstract class BoxedScalarConverter<T> implements Converter<T>  {
  
  @Override
  public final Object convertToJava(SEXP value) {
    if(!(value instanceof AtomicVector)) {
      throw new EvalException("Cannot convert '%s' to boolean", value.getTypeName());
    } 
    Vector vector = (Vector)value;
    if(vector == Null.INSTANCE || vector.isElementNA(0)) {
      return null;
    }
    return getFirstElement((Vector)value);
  }
  
  protected abstract Object getFirstElement(Vector value);

}
