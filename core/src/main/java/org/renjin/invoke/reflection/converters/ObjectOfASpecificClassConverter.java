package org.renjin.invoke.reflection.converters;

import org.renjin.sexp.ExternalPtr;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;


/**
 * Converter for a class of object that we don't do anything special
 * with: not java.lang.Object or java.lang.String, or java.lang.Number.
 */
public class ObjectOfASpecificClassConverter implements Converter<Object> {

  private Class clazz;

  public ObjectOfASpecificClassConverter(Class clazz) {
    this.clazz = clazz;
  }

  @Override
  public SEXP convertToR(Object value) {
    if(value == null) {
      return Null.INSTANCE;
    }
    return new ExternalPtr(value);
  }

  @Override
  public Object convertToJava(SEXP exp) {

    // try to simply unwrap 
    if(exp instanceof ExternalPtr) {
      ExternalPtr ptr = (ExternalPtr)exp;
      if(clazz.isAssignableFrom(ptr.getInstance().getClass())) {
        return ptr.getInstance();
      }
    }
    throw new ConversionException();
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    try {
      convertToJava(exp);
      return true;

    } catch(ConversionException e) {
      return false;
    }
  }

  @Override
  public int getSpecificity() {
    return Specificity.SPECIFIC_OBJECT;
  }
}
