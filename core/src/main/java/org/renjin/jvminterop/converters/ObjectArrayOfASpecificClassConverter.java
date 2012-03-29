package org.renjin.jvminterop.converters;

import java.util.Collection;

import org.renjin.jvminterop.ObjectFrame;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;


/**
 * Converter for a class of object that we don't do anything special
 * with: not java.lang.Object or java.lang.String, or java.lang.Number.
 */
public class ObjectArrayOfASpecificClassConverter implements Converter<Object[]> {
  public static final Converter INSTANCE = new ObjectArrayOfASpecificClassConverter();
  
  private ObjectArrayOfASpecificClassConverter() {
    
  }
  
  private Class clazz;
  
  public ObjectArrayOfASpecificClassConverter(Class clazz) {
    this.clazz = clazz;
  }
  
  @Override
  public SEXP convertToR(Object[] value) {
    if(value == null) {
      return Null.INSTANCE;
    }
    return Environment.createChildEnvironment(Environment.EMPTY, new ObjectFrame(value));
  }

  @Override
  public Object convertToJava(SEXP exp) {
    // try to simply unwrap 
    if(exp instanceof Environment) {
      Environment env = (Environment)exp;
      if(env.getFrame() instanceof ObjectFrame) {
        Object instance = ((ObjectFrame)env.getFrame()).getInstance();
        if(clazz.isAssignableFrom(instance.getClass())) {
          return instance;
        }
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
