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
package org.renjin.primitives.match;

import org.renjin.invoke.annotations.Internal;
import org.renjin.primitives.match.DuplicateSearchAlgorithm.Action;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Vector;

import java.util.HashMap;



public class Duplicates {  
 
  
  /**
   * unique returns a vector, data frame or array like x but with duplicate elements/rows removed.
   *
   * @param x a vector
   * @param incomparables a vector of values that cannot be compared. FALSE is a special value,
   *          meaning that all values can be compared, and may be the only value accepted for methods
   *        other than the default. It will be coerced internally to the same type as x.
   * @param fromLast
   * @return logical indicating if duplication should be considered from the last, i.e., the last
   *       (or rightmost) of identical elements will be kept. This only matters for names or dimnames.
   */
  @Internal
  public static Vector unique(Vector x, Vector incomparables, boolean fromLast) {
    
    return search(x, incomparables, fromLast, 
        new UniqueAlgorithm());
 
  }

  @Internal
  public static Vector duplicated(Vector x, AtomicVector incomparables, boolean fromLast) {
    
    return search(x, incomparables, fromLast, 
        new DuplicatedAlgorithm());
 
  }
  
  /**
   * Determines which elements of a vector or data frame are duplicates of elements with smaller
   * subscripts, and returns a logical vector indicating which elements (rows) are duplicates.
   * @param x a vector
   * @param incomparables a vector of values that cannot be compared. FALSE is a special value, meaning
   *        that all values can be compared, and may be the only value accepted for methods
   *      other than the default. It will be coerced internally to the same type as x.
   * @param fromLast logical indicating if duplication should be considered from the reverse side, i.e.,
   *      the last (or rightmost) of identical elements would correspond to duplicated=FALSE.
   * @return a non-negative integer (of length one).
   */
  @Internal
  public static int anyDuplicated(Vector x, AtomicVector incomparables, boolean fromLast) {

    return search(x, incomparables, fromLast,
        new AnyDuplicateAlgorithm());
  }
  
  private static <ResultType> ResultType search(
      Vector x, 
      Vector incomparables,
      boolean fromLast,
      DuplicateSearchAlgorithm<ResultType> algorithm) {
   
    algorithm.init(x);
    
    /** Maps elements -> first encountered index */
    HashMap<Object, Integer> seen = Maps.newHashMap();
   
    for(Integer index : new IndexSequence(x, fromLast)) {
      
      Object element = x.getElementAsObject(index);
      
      Integer originalIndex = seen.get(element);
      
      if(originalIndex == null) {
        algorithm.onUnique(index);
        seen.put(element, index);
      
      } else {
        if(algorithm.onDuplicate(index, originalIndex) == Action.STOP) {
          return algorithm.getResult();
        }
      }
    }
    return algorithm.getResult();
  }  
}
