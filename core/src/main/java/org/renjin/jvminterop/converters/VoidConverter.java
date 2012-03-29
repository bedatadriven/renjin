package org.renjin.jvminterop.converters;

import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;

public class VoidConverter implements Converter<Void> {

  public static final VoidConverter INSTANCE = new VoidConverter();
  
  private VoidConverter() {
  }
  
  public static boolean accept(Class clazz) {
    return clazz == Void.TYPE || clazz == Void.class;
  }
  
  @Override
  public SEXP convertToR(Void value) {
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
