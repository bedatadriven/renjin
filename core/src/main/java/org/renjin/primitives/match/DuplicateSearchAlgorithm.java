package org.renjin.primitives.match;

import org.renjin.sexp.Vector;

interface DuplicateSearchAlgorithm<ResultType> {

  enum Action {
    STOP,
    CONTINUE
  }
  
  void init(Vector source);
  
  void onUnique(int index);
  
  Action onDuplicate(int duplicateIndex, int originalIndex);
  
  ResultType getResult();
}
