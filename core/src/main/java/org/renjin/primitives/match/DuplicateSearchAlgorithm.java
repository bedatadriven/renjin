package org.renjin.primitives.match;

import r.lang.Vector;

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
