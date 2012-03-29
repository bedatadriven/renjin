package org.renjin.primitives.match;

import java.util.Iterator;

import org.renjin.sexp.Vector;


class IndexSequence implements Iterable<Integer> {

  private Vector vector;
  private boolean fromLast;
  
  public IndexSequence(Vector vector, boolean fromLast) {
    super();
    this.vector = vector;
    this.fromLast = fromLast;
  }

  @Override
  public Iterator<Integer> iterator() {
    if(fromLast) {
      return new ReverseIndexIterator(vector);
    } else {
      return new ForwardIndexIterator(vector);
    }
  }
}
