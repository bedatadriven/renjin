package org.renjin.primitives.subset;


import org.renjin.eval.EvalException;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.StringVector;

import java.util.HashMap;
import java.util.Map;

/**
 * Subscripts that select elements or dimensions using names. For example, {@code x["foo"]} or {@code x["foo", 3]}
 */
public class NameSubscript implements Subscript {
  
  private StringVector selectedNames;
  private AtomicVector sourceNames;
  private boolean allowMissing;

  private Map<String, Integer> nameMap = null;
  
  public NameSubscript(StringVector selectedNames, AtomicVector sourceNames, boolean allowMissing) {
    this.selectedNames = selectedNames;
    this.sourceNames = sourceNames;
    this.allowMissing = allowMissing;
  }

  @Override
  public int computeUniqueIndex() {
    if(sourceNames == Null.INSTANCE) {
      throw new EvalException("attempt to select less than one element");
    }
    SubsetAssertions.checkUnitLength(selectedNames);
    
    String selectedName = selectedNames.getElementAsString(0);
    for (int i = 0; i < selectedNames.length(); i++) {
      String sourceName = sourceNames.getElementAsString(i);
      if(selectedName == null) {
        if(sourceName == null) {
          return i;
        }
      } else if(selectedName.equals(sourceName)) {
        return i;
      }
    }
    
    throw new EvalException("subscript out of bounds");
  }

  @Override
  public IndexIterator computeIndexes() {
    
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
  
  private class Iterator implements IndexIterator {

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
