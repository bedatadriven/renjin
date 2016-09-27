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

import org.renjin.sexp.*;


/**
 * Converter between java.lang.Object and R expressions
 */
public class ObjectConverter implements Converter<Object> {

  public static final Converter INSTANCE = new ObjectConverter();
  
  private ObjectConverter() {
    
  }

  @Override
  public SEXP convertToR(Object value) {
    if(value == null) {
      return Null.INSTANCE;
    } else {
      Converter converter = Converters.get(value.getClass());
      return converter.convertToR(value);
    } 
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    try {
      convertToJava(exp);
      return true;
      
    } catch(ConversionException e) {
      return false;
    }
  }

  @Override
  public Object convertToJava(SEXP exp) {
    if(exp == Null.INSTANCE) {
      return null;
    }
    
    // try to simply unwrap 
    if(exp instanceof ExternalPtr) {
      ExternalPtr ptr = (ExternalPtr)exp;
      return ptr.getInstance();
    }
    
    // special case for opaque Long handle
    if(exp instanceof LongArrayVector && exp.length() == 1) {
      return ((LongArrayVector)exp).getElementAsLong(0);
    }
    
    // convert R scalars to corresponding java classes
    if(exp instanceof AtomicVector && exp.length() == 1) {
      AtomicVector vector = (AtomicVector) exp;
      if(!vector.isElementNA(0)) {
        return vector.getElementAsObject(0);
      }
    }
    
    // return as itself
    return exp;
  }

  @Override
  public int getSpecificity() {
    return Specificity.OBJECT;
  }

  public static boolean accept(Class clazz) {
    return clazz.equals(Object.class);
  }

}
