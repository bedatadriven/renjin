package org.renjin.gcc.analysis;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleExpr;

import java.util.Set;

/**
 * Maps definitions to their uses and vice-versa
 */
public class UseDefMap {
  
  private Set<Integer> localVariables = Sets.newHashSet();
  
  private Multimap<Integer, GimpleExpr> useExprMap = HashMultimap.create();
  
  public UseDefMap(GimpleFunction function) {
    for (GimpleVarDecl decl : function.getVariableDeclarations()) {
      localVariables.add(decl.getId());
    }

    
    
  }
  
}
