package r.jvmi.r2j.converters;

import r.jvmi.r2j.ObjectFrame;
import r.lang.Environment;
import r.lang.SEXP;

public class ObjectConverter implements Converter<Object> {

  public static final ObjectConverter INSTANCE = new ObjectConverter();
  
  private ObjectConverter() { 
  }
  
  @Override
  public SEXP convert(Object value) {
    return Environment.createChildEnvironment(Environment.EMPTY, new ObjectFrame(value));
  }

  @Override
  public Object convertToJava(SEXP value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getSpecificity() {
    return Specificity.OBJECT;
  }
}
