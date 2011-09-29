package r.jvmi.r2j.converters;

import r.lang.Null;
import r.lang.SEXP;

public class VoidConverter implements Converter<Void> {

  public static final VoidConverter INSTANCE = new VoidConverter();
  
  private VoidConverter() {
  }
  
  public static boolean accept(Class clazz) {
    return clazz == Void.TYPE || clazz == Void.class;
  }
  
  @Override
  public SEXP convert(Void value) {
    return Null.INSTANCE;
  }

  @Override
  public Object convertToJava(SEXP value) {
    return null;
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return true;
  }

  @Override
  public int getSpecificity() {
    return 0;
  }
}
