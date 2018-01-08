/**
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
package org.renjin.invoke.codegen.scalars;

import org.apache.commons.math.complex.Complex;
import org.renjin.invoke.codegen.GeneratorDefinitionException;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.Logical;
import org.renjin.sexp.SEXP;

import java.util.Map;

public class ScalarTypes {

  private static final ScalarTypes INSTANCE = new ScalarTypes();
  
  private Map<Class, ScalarType> types = Maps.newHashMap();
  private SexpType sexpType = new SexpType();
  
  private ScalarTypes() {
    
    types.put(Integer.TYPE, new IntegerType());
    types.put(String.class, new StringType());
    types.put(Boolean.TYPE, new BooleanType());
    types.put(Double.TYPE, new DoubleType()); 
    types.put(Float.TYPE, new FloatType()); 
    types.put(Logical.class, new LogicalType());
    types.put(Complex.class, new ComplexType());
    types.put(Byte.TYPE, new ByteType());
  }
  
  public static boolean has(Class clazz) {
    return INSTANCE.types.containsKey(clazz);
  }
  
  public static ScalarType get(Class clazz) {
    if(SEXP.class.isAssignableFrom(clazz)) {
      return INSTANCE.sexpType;
    }
    ScalarType type = INSTANCE.types.get(clazz);
    if(type == null) {
      throw new GeneratorDefinitionException(clazz.getName() + " cannot be recycled upon");
    }
    return type;
  }
}
