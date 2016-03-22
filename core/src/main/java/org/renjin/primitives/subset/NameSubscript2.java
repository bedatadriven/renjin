package org.renjin.primitives.subset;


import org.renjin.sexp.StringVector;

public class NameSubscript2 implements Subscript2 {
  
  private StringVector names;

  public NameSubscript2(StringVector names) {
    this.names = names;
  }

  @Override
  public int computeUniqueIndex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IndexIterator2 indexIterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IndexPredicate computeIndexPredicate() {
    throw new UnsupportedOperationException();
  }
}
