package org.renjin.primitives.subset;


import org.renjin.eval.EvalException;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.StringVector;

import java.util.HashMap;
import java.util.Map;

public class NameSubscript2 implements Subscript2 {
  
  private StringVector selectedNames;
  private AtomicVector sourceNames;
  private boolean allowMissing;

  private Map<String, Integer> nameMap = null;
  
  public NameSubscript2(StringVector selectedNames, AtomicVector sourceNames, boolean allowMissing) {
    this.selectedNames = selectedNames;
    this.sourceNames = sourceNames;
    this.allowMissing = allowMissing;
  }

  @Override
  public int computeUniqueIndex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IndexIterator2 computeIndexes() {
    
    buildMap();
    
    return new Iterator();
  }

  @Override
  public IndexPredicate computeIndexPredicate() {
    throw new UnsupportedOperationException();
  }
  
  private void buildMap() {
    if(nameMap == null) {
      nameMap = new HashMap<>();
      for (int i = 0; i < sourceNames.length(); i++) {
        String name = sourceNames.getElementAsString(i);
        if(!nameMap.containsKey(name)) {
          nameMap.put(name, i);  
        }
      }
    }
  }
  
  private class Iterator implements IndexIterator2 {

    private int selectedNameIndex = 0;

    @Override
    public int next() {
      if(selectedNameIndex >= selectedNames.length()) {
        return EOF;
      }
      String selectedName = selectedNames.getElementAsString(selectedNameIndex++);
      Integer index = nameMap.get(selectedName);
      if(index == null) {
        if(allowMissing) {
          return IntVector.NA;
        } else {
          throw new EvalException("subscript '%s' out of bounds", selectedName);
        }
      } 
      return index;
    }

    @Override
    public void restart() {
      selectedNameIndex = 0;
    }
  }
  
}
