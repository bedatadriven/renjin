/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.sexp;

import org.renjin.eval.EvalException;
import org.renjin.eval.Profiler;
import org.renjin.parser.NumericLiterals;
import org.renjin.repackaged.guava.collect.Iterables;

import java.util.Arrays;
import java.util.Collection;

public class StringArrayVector extends StringVector implements Iterable<String> {

  protected final String values[];


  public StringArrayVector(String[] values, AttributeMap attributes) {
    super(attributes);
    
    if(Profiler.ENABLED) {
      Profiler.memoryAllocated(32, values.length);
    }
    
    this.values = Arrays.copyOf(values, values.length, String[].class);
    assert checkDims() : "dim do not match length of object";
  }

  public StringArrayVector(String... values) {
    this(Arrays.copyOf(values, values.length, String[].class), AttributeMap.EMPTY);
  }

  public StringArrayVector(Iterable<String> properties) {
    this(Iterables.toArray(properties, String.class), AttributeMap.EMPTY);
  }

  public StringArrayVector(Collection<String> values, AttributeMap attributes) {
    this(values.toArray(new String[values.size()]), attributes);
  }

  public StringArrayVector(Collection<String> values) {
    this(values, AttributeMap.EMPTY);
  }



  @Override
  public int length() {
    return values.length;
  }

  public StringVector setLength(int newLength) {
    if(newLength == values.length) {
      return this;
    }
    String newValues[] = new String[newLength];
    for(int i=0;i!=newValues.length;++i){
      if(i < this.values.length) {
        newValues[i] = values[i];
      } else {
        newValues[i] = StringVector.NA;
      }
    }
    return new StringArrayVector(newValues);
  }

  public String getElementAsString(int index) {
    return values[index];
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public double asReal() {
    if(values.length > 0 &&
        values[0] != null &&
        values[0].length() > 0)
    {
      return NumericLiterals.parseDouble(values[0]);
    } else {
      return DoubleVector.NA;
    }
  }


  @Override
  protected StringArrayVector cloneWithNewAttributes(AttributeMap attributes) {
    return new StringArrayVector(values, attributes);
  }

  public String[] toArray() {
    return values.clone();
  }

  public static StringArrayVector coerceFrom(SEXP exp) {

    if(exp instanceof Vector) {
      return fromVector((Vector) exp);
    } else if(exp instanceof Symbol) {
      return new StringArrayVector( ((Symbol)exp).getPrintName() );
    }
    throw new EvalException("cannot coerce type '%s' to vector of type 'character'", exp.getTypeName());
  }

  public static StringArrayVector fromVector(Vector vector) {
    StringArrayVector.Builder result = new Builder();
    for(int i=0;i!=vector.length();++i) {
      result.add(vector.getElementAsString(i));
    }
    return result.build();
  }
}
