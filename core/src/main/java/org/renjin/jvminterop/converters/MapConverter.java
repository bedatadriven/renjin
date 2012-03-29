package org.renjin.jvminterop.converters;

import java.util.Map;

import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;


public class MapConverter implements Converter<Map<?,?>> {
  
  public static boolean accept(Class clazz) {
    return Map.class.isAssignableFrom(clazz);
  }

  private Converter elementConverter = RuntimeConverter.INSTANCE;
  
  @Override
  public SEXP convertToR(Map<?,?> map) {
    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
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
