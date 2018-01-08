/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.invoke.reflection.converters;

import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;

import java.util.Collection;


/**
 * Converts between Java {@link Collection} objects and R {@code list} vectors
 */
public class CollectionConverter implements Converter<Iterable> {

  //TODO Iterable maybe a special object
  public static boolean accept(Class clazz) {
    if (Collection.class.isAssignableFrom(clazz)//||Iterable.class.isAssignableFrom(clazz)
        ) {
      return true;
    }
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
