package r.jvmi.r2j.converters;

import r.lang.SEXP;

public class RuntimeConverter implements Converter<Object> {

  public static final RuntimeConverter INSTANCE = new RuntimeConverter();
  
  private RuntimeConverter() {
 
  }
  
  @Override
  public SEXP convert(Object value) {
    Converter converter = Converters.get(value.getClass());
    return converter.convert(value);
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
    return Integer.MAX_VALUE;
  }

}
