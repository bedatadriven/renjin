package org.renjin.primitives.match;

import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Vector;

public class DuplicatedAlgorithm implements DuplicateSearchAlgorithm<LogicalVector> {

  private LogicalArrayVector.Builder result;
  
  @Override
  public void init(Vector source) {
    result = new LogicalArrayVector.Builder(source.length());
  }
  
  @Override
  public void onUnique(int index) {
    result.set(index, false);
  }

  @Override
  public Action onDuplicate(int duplicateIndex, int originalIndex) {
    result.set(duplicateIndex, true);
    return Action.CONTINUE;
  }

  @Override
  public LogicalVector getResult() {
    return result.build();
  }
}
