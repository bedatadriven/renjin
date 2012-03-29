package org.renjin.jvminterop.converters;

import org.renjin.eval.EvalException;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


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
