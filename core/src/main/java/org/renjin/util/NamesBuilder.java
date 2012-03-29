package org.renjin.util;

import java.util.List;

import org.objectweb.asm.tree.IntInsnNode;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;


import com.google.common.collect.Lists;

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
      names = Lists.newArrayListWithCapacity(Math.max(initialCapacity, size));
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
      return new StringVector(names);
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
}
