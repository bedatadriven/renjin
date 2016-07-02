package org.renjin.compiler.ir.tac;

import com.google.common.collect.Lists;
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

    if(IRArgument.anyNamed(arguments)) {
      throw new UnsupportedOperationException("TODO");
    } else {
      matchPositionally(closure);
    }
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
