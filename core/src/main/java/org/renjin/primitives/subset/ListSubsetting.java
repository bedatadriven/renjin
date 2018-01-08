/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.renjin.sexp.ListVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;

class ListSubsetting {
  
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
      if (attribute != Symbols.NAMES && 
          attribute != Symbols.DIM &&
          attribute != Symbols.DIMNAMES) {
        
        result.setAttribute(attribute, list.getAttribute(attribute));
      }
    }

    return result.build();
    
  }
  
}
