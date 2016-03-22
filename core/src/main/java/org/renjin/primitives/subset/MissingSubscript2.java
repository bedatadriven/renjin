package org.renjin.primitives.subset;


import org.renjin.eval.EvalException;

public class MissingSubscript2 implements Subscript2 {
  @Override
  public int computeUniqueIndex() {
    throw new EvalException("[[ ]] with missing subscripts");
  }

  @Override
  public IndexIterator2 computeIndexes() {
    throw new UnsupportedOperationException();
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
