package org.renjin.invoke.reflection.converters;

import org.renjin.sexp.*;


/**
 * Converter between java.lang.Object and R expressions
 */
public class ObjectConverter implements Converter<Object> {

  public static final Converter INSTANCE = new ObjectConverter();
  
  private ObjectConverter() {
    
  }

  @Override
  public SEXP convertToR(Object value) {
    if(value == null) {
      return Null.INSTANCE;
    } else {
      Converter converter = Converters.get(value.getClass());
      return converter.convertToR(value);
    } 
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
  public Object convertToJava(SEXP exp) {
    if(exp == Null.INSTANCE) {
      return null;
    }
    
    // try to simply unwrap 
    if(exp instanceof ExternalPtr) {
      ExternalPtr ptr = (ExternalPtr)exp;
      return ptr.getInstance();
    }
    
    // special case for opaque Long handle
    if(exp instanceof LongArrayVector && exp.length() == 1) {
      return ((LongArrayVector)exp).getElementAsLong(0);
    }
    
    // convert R scalars to corresponding java classes
    if(exp instanceof AtomicVector && exp.length() == 1) {
      AtomicVector vector = (AtomicVector) exp;
      if(!vector.isElementNA(0)) {
        return vector.getElementAsObject(0);
      }
    }
    
    // return as itself
    return exp;
  }

  @Override
  public int getSpecificity() {
    return Specificity.OBJECT;
  }

  public static boolean accept(Class clazz) {
    return clazz.equals(Object.class)||clazz.equals(Class.class);
  }

}
