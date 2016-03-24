package org.renjin.primitives.subset;


import org.renjin.eval.EvalException;

/**
 * 
 */
class MissingSubscript implements Subscript {
  
  private int sourceLength;

  public MissingSubscript(int sourceLength) {
    this.sourceLength = sourceLength;
  }

  @Override
  public int computeUniqueIndex() {
    throw new EvalException("[[ ]] with missing subscripts");
  }

  @Override
  public IndexIterator computeIndexes() {
    return new IndexIterator() {
      
      private int i = 0;
      
      @Override
      public int next() {
        if(i >= sourceLength) {
          return EOF;
        }
        return i++;
      }

      @Override
      public void restart() {
        i = 0;
      }
    };
  }

  @Override
  public IndexPredicate computeIndexPredicate() {
    return new IndexPredicate() {
      @Override
      public boolean apply(int index) {
        return true;
      }
    };
  }
}
