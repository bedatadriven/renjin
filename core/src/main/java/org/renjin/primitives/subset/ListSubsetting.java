package org.renjin.primitives.subset;

import org.renjin.sexp.ListVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;

public class ListSubsetting {
  
  public static ListVector removeListElements(ListVector list, IndexPredicate predicate) {
    
    boolean anyRemoved = false;
    ListVector.NamedBuilder result = new ListVector.NamedBuilder(0, list.length());
    
    for (int i = 0; i < list.length(); i++) {
      if(predicate.apply(i)) {
        anyRemoved = true;
      } else {
        result.add(list.getName(i), list.getElementAsSEXP(i));
      }
    }
    
    // If no items have been removed, throw away the copy and return the original
    if(!anyRemoved) {
      return list;
    }
    
    // Copy all attributes except those related to length
    for (Symbol attribute : list.getAttributes().names()) {
      if(attribute != Symbols.NAMES && 
         attribute != Symbols.DIM &&
         attribute != Symbols.DIMNAMES) {
        
        result.setAttribute(attribute, list.getAttribute(attribute));
      }
    }

    return result.build();
    
  }
  
}
