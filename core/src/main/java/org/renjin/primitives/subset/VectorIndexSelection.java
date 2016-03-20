package org.renjin.primitives.subset;

import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbols;

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
  public IndexIterator iterator() {
    return subscript.iterator();
  }

  @Override
  public int getSourceDimensions() {
    return 1;
  }

  @Override
  public int[] getSubscriptDimensions() {
    return new int[] { subscript.getCount() };
  }

  @Override
  protected AtomicVector getNames(int dimensionIndex) {
    return (AtomicVector) source.getAttribute(Symbols.NAMES);
  }

  @Override
  public IndexIterator getSelectionAlongDimension(int dimensionIndex) {
    if(dimensionIndex != 0) {
      throw new IllegalArgumentException("dimensionIndex: " + dimensionIndex);
    }
    return iterator();
  }
}
