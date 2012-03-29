package org.renjin.primitives.subset;

import java.util.Iterator;

import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbols;


import com.google.common.collect.UnmodifiableIterator;

/**
 * When only one subscript, without dim(s) is provided, it is treated
 * as a index over a <i>vector</i>, regardless of the dimension of the 
 * source vector.
 *
 */
public class VectorIndexSelection extends Selection {
  private final SEXP source;
  private final Subscript subscript;
  
  public VectorIndexSelection(SEXP source, SEXP subscript) {
    super(source);
    this.source = source;
    this.subscript = parseSubscript(subscript, 0, source.length());
  }

  @Override
  public Iterator<Integer> iterator() {
    return new UnmodifiableIterator<Integer>() {
      private int i = 0;
      
      @Override
      public boolean hasNext() {
        return i < subscript.getCount();
      }

      @Override
      public Integer next() {
        return subscript.getAt(i++);
      }
    };
  }

  @Override
  public int getSourceDimensions() {
    return 1;
  }

  @Override
  public int getElementCount() {
    return subscript.getCount();
  }

  @Override
  public int[] getSubscriptDimensions() {
    return new int[] { getElementCount() };
  }

  @Override
  protected AtomicVector getNames(int dimensionIndex) {
    return (AtomicVector) source.getAttribute(Symbols.NAMES);
  }

  @Override
  public Iterable<Integer> getSelectionAlongDimension(int dimensionIndex) {
    if(dimensionIndex != 0) {
      throw new IllegalArgumentException("dimensionIndex: " + dimensionIndex);
    }
    return this;
  }

}
