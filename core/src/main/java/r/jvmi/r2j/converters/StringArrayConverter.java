package r.jvmi.r2j.converters;

import r.lang.AtomicVector;
import r.lang.SEXP;
import r.lang.StringVector;

public class StringArrayConverter implements Converter<String[]>{

  public static boolean accept(Class clazz) {
    return clazz.isArray() && clazz.getComponentType() == String.class;
  }
  
  @Override
  public SEXP convertToR(String[] value) {
    return new StringVector(value);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof AtomicVector;
  }

  @Override
  public Object convertToJava(SEXP value) {
    AtomicVector vector = (AtomicVector)value;
    String[] array = new String[value.length()];
    for(int i=0;i!=value.length();++i) {
      array[i] = vector.getElementAsString(i);
    }
    return array;
  }

  @Override
  public int getSpecificity() {
    return Specificity.SPECIFIC_OBJECT;
  }
}
