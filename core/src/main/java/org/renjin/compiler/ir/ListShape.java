/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.compiler.ir;

import org.renjin.sexp.*;

import java.util.Arrays;
import java.util.Objects;

public class ListShape {

  private final ValueBounds[] elements;
  private final String[] names;

  public ListShape(AtomicVector names, ValueBounds[] elements) {
    this.elements = elements;
    if(names instanceof StringVector) {
      this.names = names.toStringArray();
    } else {
      this.names = null;
    }
  }

  /**
   * @return true if the given {@code sexp} conforms to this shape.
   */
  public boolean test(SEXP sexp) {
    if (!(sexp instanceof ListVector)) {
      return false;
    }
    ListVector list = (ListVector) sexp;
    if(list.length() != elements.length) {
      return false;
    }

    if(names == null) {
      if(list.getNames() != Null.INSTANCE) {
        return false;
      }
    } else {
      String listNames[] = list.getNames().toStringArray();
      if(!Arrays.equals(names, listNames)) {
        return false;
      }
    }

    for (int i = 0; i < elements.length; i++) {
      if(!elements[i].test(list.getElementAsSEXP(i))) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns the index of this list's shape corresponding to the given name,
   * or -1 if this list has no such name.
   */
  public int getElementIndex(String elementName) {
    if(names != null) {
      for (int i = 0; i < names.length; i++) {
        if (Objects.equals(elementName, names[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  public ValueBounds getElementBounds(int elementIndex) {
    return elements[elementIndex];
  }
}
