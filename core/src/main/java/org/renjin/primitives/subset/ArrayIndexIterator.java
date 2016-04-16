package org.renjin.primitives.subset;

import org.renjin.sexp.IntVector;

import static org.renjin.sexp.IntVector.isNA;

/**
 * Iterator over a sequence of indices described by array-style subscripts.
 * 
 * <p>Given a tuple of subscripts, such as {@code [3,4]} or {@code [, TRUE]} or 
 * {@code [,,3]}, this iterator will iterate over the selected vector indices.</p>
 */
class ArrayIndexIterator implements IndexIterator {
  
  private static final int STATE_BEGIN = 0;
  private static final int STATE_RUNNING = 1;
  private static final int STATE_END = 2;
  
  private final int numDim;
  private IndexIterator iterators[];
  
  private int[] increments;
  
  private int[] offsetStack;
  
  private int state = STATE_BEGIN;

  public ArrayIndexIterator(int dim[], Subscript[] subscripts) {
    this.numDim = subscripts.length;
    
    // Initialize the iterators over each dimension
    // We reverse them so that we go from COL -> ROW
    this.iterators = new IndexIterator[subscripts.length];
    for (int i = 0; i < numDim; i++) {
      this.iterators[i] = subscripts[i].computeIndexes();
    }
    
    // Compute the dimension increments
    // For example, a matrix has two dimensions: column -> row
    // columns 
    increments = new int[numDim];
    increments[0] = 1; // rows 
    for(int d=1;d<numDim;++d) {
      increments[d] = increments[d-1] * dim[d-1];
    }
    
    // Allocate a stack of offset, so that, for example,
    // as we iterate over selected rows in the column,
    // we can remember the start position of the row.
    offsetStack = new int[numDim+1];
  }

  @Override
  public int next() {
    if(state == STATE_END) {
      return EOF;
    }
    
    if(state == STATE_BEGIN) {
      // Initialize from COL --> ROW
      int firstIndex = 0;
      for (int d = numDim - 1; d >= 0; d--) {
        int index = iterators[d].next();

        // if any of the dimensions has no elements, than the selection
        // is empty
        if (index == EOF) {
          state = STATE_END;
          return EOF;
        }
        if(isNA(index)) {
          firstIndex = IntVector.NA;
        } else if(!isNA(firstIndex)) {
          firstIndex += (index * increments[d]);
        }
        offsetStack[d] = firstIndex;
      }
      state = STATE_RUNNING;
      return firstIndex;
    }
    
    // First, Move from ROW --> COL until we find a dimension
    // with more elements
    int d = 0;
    while(true) {
      int nextIndex = iterators[d].next();
      if(nextIndex != EOF) {
        updateOffset(d, nextIndex);
        break;
      } 
      iterators[d].restart();
      d = d + 1;
      if(d >= numDim) {
        state = STATE_END;
        return EOF;
      }
    }
    
    // If neccesary, move from COL -- > ROW to get the first
    // elements of downstream dimensions
    while(d > 0) {
      d = d - 1;
      int nextIndex = iterators[d].next();
      updateOffset(d, nextIndex);
    }
    
    return offsetStack[0];
  }

  private void updateOffset(int dimensionIndex, int nextIndex) {
    int offset = offsetStack[dimensionIndex + 1];
    if(isNA(offset) || isNA(nextIndex)) {
      offsetStack[dimensionIndex] = IntVector.NA;
    } else {
      offsetStack[dimensionIndex] = offset + (nextIndex * increments[dimensionIndex]);
    }
  }

  @Override
  public void restart() {
    throw new UnsupportedOperationException();
  }
}
