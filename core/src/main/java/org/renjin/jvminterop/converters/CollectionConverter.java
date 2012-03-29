package org.renjin.jvminterop.converters;

import java.util.Collection;

import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;


public class CollectionConverter implements Converter<Iterable> {

  //TODO Iterable maybe a special object
  public static boolean accept(Class clazz) {
    if (Collection.class.isAssignableFrom(clazz)//||Iterable.class.isAssignableFrom(clazz)
        )
      return true;
    return false;
    //return false; || Iterable.class.isAssignableFrom(clazz)
  }

  private Converter elementConverter = RuntimeConverter.INSTANCE;

  @Override
  public SEXP convertToR(Iterable collection) {
    ListVector.Builder list = new ListVector.Builder();
    for (Object element : collection) {
      list.add(elementConverter.convertToR(element));
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
//    return exp instanceof ListVector;
  }

  @Override
  public int getSpecificity() {
    return Specificity.COLLECTION;
  }
}
