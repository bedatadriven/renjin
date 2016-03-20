package org.renjin.primitives.subset;

import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;

/**
 * Selection of the entire source vector
 * through the use of x[]
 */
public class CompleteSelection extends Selection {

  private SEXP source;
  private AtomicVector sourceDim;

  public CompleteSelection(SEXP source) {
    super(source);
    this.source = source;
    this.sourceDim = (AtomicVector) source.getAttributes().getDim();
  }

  @Override
  public IndexIterator iterator() {
    return new IndexIterator() {
      private int i=0;

      @Override
      public boolean hasNext() {
        return i < source.length();
      }

      @Override
      public int next() {
        return i++;
      }
    };
  }

  @Override
  public int getSourceDimensions() {
    if(sourceDim.length() == 0) {
      return 1;
    } else {
      return sourceDim.length();
    }
  }

  @Override
  public int[] getSubscriptDimensions() {
    if(sourceDim.length() == 0) {
      return new int[] { source.length() };
    } else {
      return ((IntVector)sourceDim).toIntArray();
    }
  }

  private int getDimensionLength(int d) {
    if(sourceDim.length() == 0 && d == 0) {
      return source.length();
    } else {
      return sourceDim.getElementAsInt(d);
    }
  }

  @Override
  protected AtomicVector getNames(int dimensionIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IndexIterator getSelectionAlongDimension(int dimensionIndex) {
    final int length = getDimensionLength(dimensionIndex);

    return new IndexIterator() {
      private int i = 0;
      @Override
      public boolean hasNext() {
        return i < length;
      }

      @Override
      public int next() {
        return i++;
      }
    };
  }
}
