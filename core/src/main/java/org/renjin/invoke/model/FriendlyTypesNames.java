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
package org.renjin.invoke.model;

import org.renjin.sexp.*;

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
    names.put(LogicalArrayVector.class, LogicalVector.TYPE_NAME);
    names.put(Logical.class, LogicalVector.TYPE_NAME);
    names.put(Boolean.class, LogicalVector.TYPE_NAME);
    names.put(Boolean.TYPE, LogicalVector.TYPE_NAME);
    names.put(IntArrayVector.class, IntVector.TYPE_NAME);
    names.put(Integer.class, IntVector.TYPE_NAME);
    names.put(Integer.TYPE, IntVector.TYPE_NAME);
    names.put(DoubleArrayVector.class, DoubleVector.TYPE_NAME);
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
