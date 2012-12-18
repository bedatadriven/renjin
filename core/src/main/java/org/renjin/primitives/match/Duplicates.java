package org.renjin.primitives.match;

import java.util.HashMap;

import org.renjin.eval.EvalException;
import org.renjin.primitives.match.DuplicateSearchAlgorithm.Action;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.Vector;

import com.google.common.collect.Maps;



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
  public static Vector unique(Vector x, Vector incomparables, boolean fromLast) {
    
    return search(x, incomparables, fromLast, 
        new UniqueAlgorithm());
 
  }

  public static Vector duplicated(AtomicVector x, AtomicVector incomparables, boolean fromLast) {
    
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
