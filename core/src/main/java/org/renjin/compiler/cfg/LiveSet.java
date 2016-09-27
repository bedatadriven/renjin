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
package org.renjin.compiler.cfg;

import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Statement;

import java.util.Collection;

/**
 * Computes whether variables are live at different points in the CFG.
 * 
 * @see <a href="http://www.rw.cdl.uni-saarland.de/~grund/papers/cgo08-liveness.pdf">
 *   Fast Liveness Checking for SSA-Form Programs</a>
 */
public class LiveSet {
  private DominanceTree tree;
  private UseDefMap useDefMap;

  public LiveSet(DominanceTree tree, UseDefMap useDefMap) {
    this.tree = tree;
    this.useDefMap = useDefMap;
  }

  public boolean isLiveOut(BasicBlock q, Statement s, LValue a) {
    
    // Trivially check if a is used in the same basic block
    int i = q.getStatements().indexOf(s) + 1;
    while(i < q.getStatements().size()) {
      if(uses(q.getStatements().get(i), a)) {
        // used in the same basic block.
        return true;
      }
    }
    
    return isLiveOut(q, a);
  }
  
  
  /**
   * A variable {@code a} is live-out at a node {@code q} if it is live-in at 
   * a successor of {@code q}
   */
  public boolean isLiveOut(BasicBlock q, LValue a) {

    BasicBlock def = def(a);
    if(def == q) {
      // return uses(a) \ def(a) != 0
      
      // If a is defined in this block,
      // we only have to trivially check to see if it is used outside of def(a)
      
      for (BasicBlock use : uses(a)) {
        if (use != def) {
          return true;
        }
      }
      return false;

    } 
    
    if(tree.strictlyDominates(def, q)) {
      // T_(q,a) ← T_q ∩ sdom(def(a))
      // for t ∈ T(q,a) do
      //   U ← uses(a)
      //   if t = q and q is no back edge target then U ← U \ {q}
      //   if Rt ∩ U 6= ∅ then return true
      throw new UnsupportedOperationException("TODO");
    }
    
    return false;
  }
  
  private Collection<BasicBlock> uses(LValue a) {
    return useDefMap.getUsedBlocks(a);
  }
  
  private BasicBlock def(LValue a) {
    return useDefMap.getDefinitionBlock(a);
  }

  private boolean uses(Statement statement, LValue a) {
    if(statement.getRHS().equals(a)) {
      return true;
    } else {
      for (int i=0; i < statement.getChildCount(); ++i) {
        if(statement.childAt(i).equals(a)) {
          return true;
        }
      }
    }
    return false;
  }
}
