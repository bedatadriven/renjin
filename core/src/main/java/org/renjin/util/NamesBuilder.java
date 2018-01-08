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
package org.renjin.util;

import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class to build names attribute
 *
 */
public final class NamesBuilder {
  
  private List<String> names = null;
  private int initialCapacity;
  private int size;
  
  private NamesBuilder(int initialSize, int initialCapacity) {
    this.size = initialSize;
    this.initialCapacity = initialCapacity;
  }
  
  private NamesBuilder(SEXP exp) {
    Vector namesVector = exp.getNames();
    if(namesVector == Null.INSTANCE) {
      this.initialCapacity = this.size = exp.length();
    } else {
      names = Lists.newArrayList(((StringVector)namesVector));
      size = names.size();
    }
  }
  
  /**
   * @return true if the names vector in progress has any non-NA
   * elements.
   */
  public boolean haveNames() {
    return names != null;
  }
  
  public void add(String name) {
    set(size, name);
  }
  
  public void addBlank() {
    set(size, "");
  }
  
  public void set(int index, String name) {
    if(names == null && "".equals(name)) {
      // no actual data yet, just
      // maintain our internal count
      if(index+1 > size) {
        size = index+1;
      }
    } else {
      ensureSize(index+1);
      names.set(index, name);
    }
  }

  private void ensureSize(int newSize) {
    if(names == null) {
      names = Lists.newArrayListWithCapacity(Math.max(initialCapacity, newSize));
    }
    while(newSize > names.size()) {
      names.add("");
    }
    size = names.size();
    assert size == newSize;
  }

  public Vector build(int length) {
    if(names == null) {
      return Null.INSTANCE;
    } else {
      assert names.size() <= length;
      while(names.size() < length) {
        names.add("");
      }
      return new StringArrayVector(names);
    }
  }

  /**
   * Builds a names StringVector of the given {@code length}, 
   * even if the names vector is empty.
   */
  public Vector buildEvenIfEmpty(int length) {
    if(names == null) {
      names = new ArrayList<String>();
    }
    while(names.size() < length) {
      names.add("");
    }
    return new StringArrayVector(names);
  }
  
  public Vector build() {
    if(names == null) {
      return Null.INSTANCE;
    } else {
      return new StringArrayVector(names);
    }
  }
  
  public static NamesBuilder withInitialCapacity(int initialCapacity) {
    return new NamesBuilder(0, initialCapacity);
  }
  
  public static NamesBuilder clonedFrom(SEXP exp) {
    return new NamesBuilder(exp);
  }

  public static NamesBuilder withInitialLength(int initialLength) {
    return new NamesBuilder(initialLength, initialLength);
  }

  public int length() {
    return size;
  }

  public void remove(int index) {
    if(names != null) {
      names.remove(index);
    }
  }
  
  public int getIndexByName(String name) {
    if(names == null) {
      return -1;
    } else {
      return names.indexOf(name);
    }
  }
}
