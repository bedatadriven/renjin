package org.renjin.primitives.subset;

import org.renjin.sexp.LogicalVector;

public class LogicalSubscript2 implements Subscript2 {
  
  private LogicalVector vector;

  public LogicalSubscript2(LogicalVector vector) {
    this.vector = vector;
  }

  @Override
  public int computeUniqueIndex() {
    // In the context of the [[ operator, we treat logical subscripts as integers
    Selections.checkUnitLength(vector);
    
    return vector.getElementAsInt(0);
  }

  @Override
  public IndexIterator2 indexIterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IndexPredicate computeIndexPredicate() {
    return new IndexPredicate() {
      @Override
      public boolean apply(int index) {
        int mask = vector.getElementAsInt(index % vector.length());
        return mask == 1;
      }
    };
  }

}
