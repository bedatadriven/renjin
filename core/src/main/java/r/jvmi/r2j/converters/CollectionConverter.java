package r.jvmi.r2j.converters;

import r.lang.ListVector;
import r.lang.SEXP;

public class CollectionConverter implements Converter<Iterable> {

  public static boolean accept(Class clazz) {
    return Iterable.class.isAssignableFrom(clazz);
  }

  private Converter elementConverter = RuntimeConverter.INSTANCE;
  
  @Override
  public SEXP convert(Iterable collection) {
    ListVector.Builder list = new ListVector.Builder();
    for(Object element : collection) {
      list.add(elementConverter.convert(element));
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
    return Specificity.COLLECTION;
  }
}
