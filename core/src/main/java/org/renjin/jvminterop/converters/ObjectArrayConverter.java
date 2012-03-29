package org.renjin.jvminterop.converters;

import org.renjin.jvminterop.ClassBinding;
import org.renjin.jvminterop.ClassFrame;
import org.renjin.jvminterop.ObjectFrame;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;


/**
 * Converter between java.lang.Object and R expressions
 */
public class ObjectArrayConverter implements Converter<Object[]> {

  public static final Converter INSTANCE = new ObjectArrayConverter();
  
  private ObjectArrayConverter() {
    
  }

  @Override
  public SEXP convertToR(Object[] value) {
    if(value == null) {
      return Null.INSTANCE;
    }else if(value.getClass().isArray()&&value.getClass().getComponentType()==Class.class) {
      return Environment.createChildEnvironment(Environment.EMPTY, new ClassFrame(ClassBinding.get(value.getClass().getComponentType())));
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
      Object object[]= new Object[vector.length()];
      for(int i =0;i<vector.length();i++){
        object[i] = vector.getElementAsObject(i);
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
