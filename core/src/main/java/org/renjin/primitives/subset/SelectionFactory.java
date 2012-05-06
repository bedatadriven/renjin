package org.renjin.primitives.subset;

import java.util.List;

import org.renjin.sexp.PairList.Node;
import org.renjin.sexp.SEXP;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public final class SelectionFactory {

  public static Selection fromSubscripts(SEXP source, Iterable<SEXP> subscripts) {
    // how the subscripts are interpreted depends both on how many
    // and what kind of subscripts are provided, and the dimension of the
    // source vector
     
    if(Iterables.isEmpty(subscripts)) {  
      return new CompleteSelection(source);
    
    } else if(Iterables.size(subscripts) == 1) {
      
      return fromSubscript(source, subscripts.iterator().next());
      
    } else {
      
      // otherwise we have multiple subscripts, and we treat each subscript
      // as applying as whole to its dimensions (including whole rows or columns
      // in the case of matrices)
      
      return new DimensionSelection(source, Lists.newArrayList(subscripts)); 
    }
  }
  

  public static Selection fromSubscript(SEXP source, SEXP subscript) {
    
    // if the single argument is a matrix or greater, then
    // we treat it as a matrix of coordinates
    if(CoordinateMatrixSelection.isCoordinateMatrix(source, subscript)) {
      
      return new CoordinateMatrixSelection(source, subscript);
    
    } else {
    
      // otherwise we treat the source
      // as a vector, regardless of whether it has dimensions or not
      
      return new VectorIndexSelection(source, subscript);
    }
  }
}
