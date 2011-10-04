package r.jvmi.r2j.converters;

import r.lang.Null;
import r.lang.SEXP;

public class NullConverter implements Converter<SEXP> {

  public static final NullConverter INSTANCE = new NullConverter();
  
  private NullConverter() {
    
  }
  
  @Override
  public SEXP convertToR(SEXP value) {
    return value;
  }

  @Override
  public Object convertToJava(SEXP value) {
    return value;
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    // not sure if this is useful to implement...
    return exp == Null.INSTANCE;
  }

  @Override
  public int getSpecificity() {
    return 0;
  }

}
