package r.jvmi.r2j.converters;

import java.util.Map;

import r.lang.ListVector;
import r.lang.SEXP;

public class MapConverter implements Converter<Map<?,?>> {
  
  public static boolean accept(Class clazz) {
    return Map.class.isAssignableFrom(clazz);
  }

  private Converter elementConverter = RuntimeConverter.INSTANCE;
  
  @Override
  public SEXP convertToR(Map<?,?> map) {
    ListVector.Builder list = new ListVector.Builder();
    for(Map.Entry<?,?> entry : map.entrySet()) {
      list.add(
          entry.getKey().toString(), 
          elementConverter.convertToR(entry.getValue()));
    }
    return list.build();
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
    return Specificity.MAP;
  } 
}
