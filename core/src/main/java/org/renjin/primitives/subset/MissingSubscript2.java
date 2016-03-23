package org.renjin.primitives.subset;


import org.renjin.eval.EvalException;

public class MissingSubscript2 implements Subscript2 {
  
  private int sourceLength;

  public MissingSubscript2(int sourceLength) {
    this.sourceLength = sourceLength;
  }

  @Override
  public int computeUniqueIndex() {
    throw new EvalException("[[ ]] with missing subscripts");
  }

  @Override
  public IndexIterator2 computeIndexes() {
    return new IndexIterator2() {
      
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
