package org.renjin.primitives.match;

import org.renjin.sexp.Vector;

public class UniqueAlgorithm implements DuplicateSearchAlgorithm<Vector> {

  private Vector source;
  private boolean[] unique;
  private int uniqueCount;
  
  @Override
  public void init(Vector source) {
    this.source = source;
    this.unique = new boolean[source.length()];
  }

  @Override
  public void onUnique(int index) {
    unique[index] = true;
    uniqueCount++;
  }

  @Override
  public Action onDuplicate(int duplicateIndex, int originalIndex) {
    return Action.CONTINUE;
  }

  @Override
  public Vector getResult() {
    Vector.Builder result = source.newBuilderWithInitialCapacity(uniqueCount);
    for(int i=0;i!=unique.length;++i) {
      if(unique[i]) {
        result.addFrom(source, i);
      }
    }
    
    return result.build();
  }
}
