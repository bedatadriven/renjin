package r.jvmi.r2j.converters;

import r.jvmi.r2j.ObjectFrame;
import r.lang.Environment;
import r.lang.Null;
import r.lang.SEXP;

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
