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

import r.lang.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts Java class to "R-friendly" names
 */
class FriendlyTypesNames {

  private static FriendlyTypesNames INSTANCE = null;

  private Map<Class, String> names;

  private FriendlyTypesNames() {
    names = new HashMap<Class, String>();
    names.put(SEXP[].class, "...");
    names.put(SEXP.class, "any");
    names.put(LogicalVector.class, LogicalVector.TYPE_NAME);
    names.put(Logical.class, LogicalVector.TYPE_NAME);
    names.put(Boolean.class, LogicalVector.TYPE_NAME);
    names.put(Boolean.TYPE, LogicalVector.TYPE_NAME);
    names.put(IntVector.class, IntVector.TYPE_NAME);
    names.put(Integer.class, IntVector.TYPE_NAME);
    names.put(Integer.TYPE, IntVector.TYPE_NAME);
    names.put(DoubleVector.class, DoubleVector.TYPE_NAME);
    names.put(Double.class, DoubleVector.TYPE_NAME);
    names.put(Double.TYPE, DoubleVector.TYPE_NAME);
    names.put(String.class, StringVector.TYPE_NAME);
    names.put(StringVector.class, StringVector.TYPE_NAME);
    names.put(ListVector.class, ListVector.TYPE_NAME);
    names.put(PairList.Node.class, PairList.TYPE_NAME);
  }

  public static FriendlyTypesNames get() {
    if(INSTANCE == null) {
      INSTANCE = new FriendlyTypesNames();
    }
    return INSTANCE;
  }

  public String format(Class clazz) {
    if(names.containsKey(clazz)) {
      return names.get(clazz);
    } else {
      return clazz.getSimpleName();
    }
  }
}
