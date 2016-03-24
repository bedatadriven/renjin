package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.LogicalVector;

public class LogicalSubscript2 implements Subscript2 {
  
  private LogicalVector subscript;
  private int sourceLength;

  public LogicalSubscript2(LogicalVector subscript, int sourceLength) {
    this.subscript = subscript;
    this.sourceLength = sourceLength;
  }

  @Override
  public int computeUniqueIndex() {
    // In the context of the [[ operator, we treat logical subscripts as integers
    SubsetAssertions.checkUnitLength(subscript);

    int oneBaseIndex = subscript.getElementAsInt(0);
    
    if(oneBaseIndex == 0) {
      throw new EvalException("attempt to select less than one element");
    }

    return oneBaseIndex - 1;
  }

  @Override
  public IndexIterator2 computeIndexes() {
    if(subscript.length() == 0) {
      return EmptyIndexIterator2.INSTANCE;
    }
    return new Iterator();
  }

  @Override
  public IndexPredicate computeIndexPredicate() {
    return new IndexPredicate() {
      @Override
      public boolean apply(int index) {
        int mask = subscript.getElementAsInt(index % subscript.length());
        return mask == 1;
      }
    };
  }
  
  
  private class Iterator implements IndexIterator2 {

    int nextIndex = 0;

    @Override
    public int next() {
      while(true) {
        if(nextIndex >= sourceLength) {
          return EOF;
        }
        int sourceIndex = nextIndex++;
        int subscriptValue = subscript.getElementAsRawLogical(sourceIndex % subscript.length());
        if(subscriptValue == 1) {
          return sourceIndex;
        }
        if(IntVector.isNA(subscriptValue)) {
          return IntVector.NA;
        }
      }
    }

    @Override
    public void restart() {
      nextIndex = 0;
    }
  }
}
