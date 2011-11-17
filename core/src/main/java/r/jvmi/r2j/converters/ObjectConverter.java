package r.jvmi.r2j.converters;

import r.jvmi.r2j.ClassBinding;
import r.jvmi.r2j.ClassFrame;
import r.jvmi.r2j.ObjectFrame;
import r.lang.AtomicVector;
import r.lang.Environment;
import r.lang.Null;
import r.lang.SEXP;

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
    } else if(value instanceof Class) {
      return Environment.createChildEnvironment(Environment.EMPTY, new ClassFrame(ClassBinding.get((Class)value)));
    } else {
      Converter converter = Converters.get(value.getClass());
      return converter.convertToR(value);
    } 
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return true;
  }

  @Override
  public Object convertToJava(SEXP exp) {
    if(exp == Null.INSTANCE) {
      return null;
    }
    
    // try to simply unwrap 
    if(exp instanceof Environment) {
      Environment env = (Environment)exp;
      if(env.getFrame() instanceof ObjectFrame) {
        return ((ObjectFrame)env.getFrame()).getInstance();
      } else if(env.getFrame() instanceof ClassFrame) {
        return ((ClassFrame)env.getFrame()).getBoundClass();
      }
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
    return clazz.equals(Object.class);
  }

}
