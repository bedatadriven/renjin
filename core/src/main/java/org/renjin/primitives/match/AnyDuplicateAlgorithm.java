package org.renjin.primitives.match;

import org.renjin.sexp.Vector;

public class AnyDuplicateAlgorithm implements DuplicateSearchAlgorithm<Integer> {

  public int firstDuplicatedIndex;

  @Override
  public void init(Vector source) {
    firstDuplicatedIndex = 0;
  }  
  
  @Override
  public void onUnique(int index) { }

  @Override
  public Action onDuplicate(int duplicateIndex, int originalIndex) {
    firstDuplicatedIndex = duplicateIndex+1; // result is one-based 
    return Action.STOP;
  }

  @Override
  public Integer getResult() {
    return firstDuplicatedIndex;
  }
}
