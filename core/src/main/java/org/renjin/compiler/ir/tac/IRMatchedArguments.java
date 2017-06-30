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
package org.renjin.compiler.ir.tac;

import org.renjin.compiler.NotCompilableException;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Matching between supplied arguments to a closure call and the
 * formally declared arguments of the closure.
 */
public class IRMatchedArguments {

  private final List<IRArgument> arguments;
  
  private final Map<Symbol, Integer> matchedFormals = new HashMap<>();
  
  private final List<Integer> extraArguments = Lists.newArrayList();

  public IRMatchedArguments(Closure closure, List<IRArgument> arguments) {

    this.arguments = arguments;

    matchPositionally(closure);
  }

  public Set<Symbol> getSuppliedFormals() {
    return matchedFormals.keySet();
  }
  
  public Map<Symbol, Integer> getMatchedFormals() {
    return matchedFormals;
  }
  
  public boolean hasExtraArguments() {
    return !extraArguments.isEmpty();
  }
  
  private void matchPositionally(Closure closure) {
    assert !IRArgument.anyNamed(arguments);

    int suppliedIndex = 0;
    int suppliedCount = arguments.size();
    
    PairList formal = closure.getFormals();
    
    /// MATCH ARGUMENTS POSITIONALLY TO FORMALS
    while(suppliedIndex < suppliedCount && formal != Null.INSTANCE) {
      PairList.Node formalNode = (PairList.Node) formal;
      if(formalNode.getTag() != Symbols.ELLIPSES) {
        
        matchedFormals.put(formalNode.getTag(), suppliedIndex);
        suppliedIndex++;
      }
      formal = formalNode.getNext();
    }
    
    /// IF THERE ARE ANY REMAINING SUPPLIED ARGUMENTS THEY FORM THE '...' LIST
    while(suppliedIndex < suppliedCount) {
      extraArguments.add(suppliedIndex++);
    }
  }
}
