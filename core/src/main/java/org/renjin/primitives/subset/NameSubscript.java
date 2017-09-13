/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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

  @Override
  public int computeCount() {
    int count = 0;
    IndexIterator it = computeIndexes();
    while(it.next() != IndexIterator.EOF) {
      count++;
    }
    return count;
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
