/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

import org.renjin.sexp.SEXP;

/**
 * Provides {@link Converter} instances
 */
public class Converters  {

  /**
   * @param clazz
   * @return a converter that is capable of converting instances of {@code clazz} to an S-expression.
   */
  public static Converter get(Class clazz) {
    if(StringConverter.accept(clazz)) {
      return StringConverter.INSTANCE;
    
    } else if(BooleanConverter.accept(clazz)) {
      return BooleanConverter.INSTANCE;
    
    } else if(IntegerConverter.accept(clazz)) {
      return IntegerConverter.INSTANCE;
      
    } else if(LongConverter.accept(clazz)) {
      return LongConverter.INSTANCE;

    } else if(FloatConverter.accept(clazz)) {
      return FloatConverter.INSTANCE;
      
    } else if(DoubleConverter.accept(clazz)) {
      return DoubleConverter.INSTANCE;
    
    } else if(SexpConverter.acceptsJava(clazz)) {
      return new SexpConverter(clazz);
      
    } else if(EnumConverter.accept(clazz)) {
      return new EnumConverter(clazz);
      
    } else if(CollectionConverter.accept(clazz)) {
      return new CollectionConverter();
      
    } else if(StringArrayConverter.accept(clazz)) {
      return StringArrayConverter.INSTANCE;
      
    }else if(BooleanArrayConverter.accept(clazz)) {
      return BooleanArrayConverter.INSTANCE;
      
    } else if(IntegerArrayConverter.accept(clazz)) {
      return IntegerArrayConverter.INSTANCE;

    } else if(LongArrayConverter.LONG_ARRAY.accept(clazz)) {
      return LongArrayConverter.LONG_ARRAY;

    } else if(FloatArrayConverter.FLOAT_ARRAY.accept(clazz)) {
      return FloatArrayConverter.FLOAT_ARRAY;

    } else if(DoubleArrayConverter.DOUBLE_ARRAY.accept(clazz)) {
      return DoubleArrayConverter.DOUBLE_ARRAY;

    } else if(BoxedDoubleArrayConverter.BOXED_DOUBLE_ARRAY.accept(clazz)) {
      return BoxedDoubleArrayConverter.BOXED_DOUBLE_ARRAY;

    } else if(BoxedFloatArrayConverter.BOXED_FLOAT_ARRAY.accept(clazz)) {
      return BoxedFloatArrayConverter.BOXED_FLOAT_ARRAY;
      
    } else if(ObjectConverter.accept(clazz)) {
      return ObjectConverter.INSTANCE;
      
    } else {
      return new ObjectOfASpecificClassConverter(clazz);
    }
  }

  public static SEXP fromJava(Object obj) {
    return get(obj.getClass()).convertToR(obj);
  }
}
