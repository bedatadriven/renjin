/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
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

package r.lang.primitive.binding;

import r.lang.AtomicExp;
import r.lang.SEXP;

import java.util.ArrayList;
import java.util.List;

public class TypeConverter {

  public static <T> T convert(SEXP value, Class<T> expType) {
    if(expType.isAssignableFrom(value.getClass())) {
      return (T) value;
    } else if(value instanceof AtomicExp && AtomicExp.isAtomic(expType)) {
      Class<AtomicExp> atomicClass = (Class<AtomicExp>)expType;
      Class elementClass = AtomicExps.elementClassOf(atomicClass);
      AtomicBuilder builder = AtomicBuilders.createFor(elementClass, value.length());
      AtomicAccessor accessor = AtomicAccessors.create(value, elementClass);
      for(int i=0;i!=accessor.length();++i) {
        if(accessor.isNA(i)) {
          builder.setNA(i);
        } else {
          builder.set(i, accessor.get(i));
        }
      }
      return (T) builder.build();
    } else {
      throw new IllegalArgumentException("Unsupported conversion from '" +
        FriendlyTypesNames.get().format(value.getClass()) + "' to '" +
        FriendlyTypesNames.get().format(expType) + "'");
    }
  }

  public static <T> List<T> convertElements(Iterable<SEXP> values, Class<T> expType) {
    List<T> items = new ArrayList<T>();
    for(SEXP exp : values) {
      items.add(convert(exp, expType));
    }
    return items;
  }
}
