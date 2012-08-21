package org.renjin.util;

import com.google.common.collect.Lists;
import org.renjin.sexp.*;

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
  
  public void addNA() {
    set(size, StringVector.NA);
  }
  
  public void set(int index, String name) {
    if(names == null && StringVector.isNA(name)) {
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
