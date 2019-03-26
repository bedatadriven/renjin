/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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


import org.renjin.repackaged.guava.collect.HashMultimap;
import org.renjin.repackaged.guava.collect.Multimap;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.util.DebugGraph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DominanceTree {

  private final Graph cfg;
  private final HashMultimap<BasicBlock, BasicBlock> Dom = HashMultimap.create();
  private final Multimap<BasicBlock, BasicBlock> dominanceFrontier = HashMultimap.create();
  private final Multimap<BasicBlock, BasicBlock> successors = HashMultimap.create();
  private final Multimap<BasicBlock, BasicBlock> predecessors = HashMultimap.create();

  public DominanceTree(Graph cfg) {
    this.cfg = cfg;
    computeDominators();
    buildTree();
    calculateDominanceFrontiers();
  }

  private void computeDominators() {

    // See http://en.wikipedia.org/wiki/Lengauer-Tarjan%27s_algorithm#Algorithms

    // dominator of the start node is the start itself
    Dom.put(cfg.getEntry(), cfg.getEntry());

    // for all other nodes, set all nodes as the dominators
    for(BasicBlock n : cfg.getBasicBlocks()) {
      if(n != cfg.getEntry()) {
        Dom.putAll(n, cfg.getBasicBlocks());
      }
    }

    // iteratively eliminate nodes that are not dominators
    boolean changes;
    do {
      changes = false;
      for(BasicBlock n : cfg.getBasicBlocks()) {
        if (n != cfg.getEntry()) {
          // Dom(n) = {n} union with intersection over all p in pred(n) of Dom(p)
          Set<BasicBlock> newDom = computeNewDominance(n);
          Set<BasicBlock> original = Dom.get(n);

          if (!original.equals(newDom)) {
            Dom.replaceValues(n, newDom);
            changes = true;
          }
        }
      }
    } while(changes);
  }

  /**
   * Find the intersection of all the dominance frontiers of all the predecessors of this
   * node, and union that with the node itself.
   */
  private Set<BasicBlock> computeNewDominance(BasicBlock n) {

    Set<BasicBlock> set = new HashSet<>();
    boolean first = true;
    for (BasicBlock predecessor : cfg.getPredecessors(n)) {
      Set<BasicBlock> frontier = Dom.get(predecessor);
      if(first) {
        set.addAll(frontier);
      } else {
        set.retainAll(frontier);
      }
      first = false;
    }
    
    set.add(n);
      
    return set;
  }

  private void buildTree() {
    for(BasicBlock n : cfg.getBasicBlocks()) {
      BasicBlock d = calculateImmediateDominator(n);
      if(d != null) {
        successors.put(d, n);
        predecessors.put(n, d);
      }
    }
  }
  
  private BasicBlock calculateImmediateDominator(BasicBlock n) {
    for(BasicBlock d : strictDominators(n)) {
      if(dominatesImmediately(d, n)) {
        return d;
      }
    }
    return null;
  }
  
  public BasicBlock getImmediateDominator(BasicBlock n) {
    Collection<BasicBlock> parent = predecessors.get(n);
    if(parent.size() != 1) {
      throw new IllegalArgumentException(n.toString());
    }
    return parent.iterator().next();
  }
  
  public boolean dominatesImmediately(BasicBlock d, BasicBlock n) {
    for(BasicBlock otherDominator : strictDominators(n)) {
      if(strictlyDominates(d, otherDominator)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * A node d strictly dominates a node n if d dominates n and d does not equal n.
   */
  public boolean strictlyDominates(BasicBlock d, BasicBlock n) {
    return !d.equals(n) && dominates(d, n);
  }
  
  /**
   * A node d dominates a node n if every path 
   * from the start node to n must go through d. 
   * By definition, every node dominates itself.
   */
  private boolean dominates(BasicBlock d, BasicBlock n) {
    if(d == n) {
      return true;
    } else {
      return Dom.containsEntry(n, d);
    }
  }

  private Iterable<BasicBlock> strictDominators(BasicBlock n) {
    return Sets.difference(Dom.get(n), Collections.singleton(n));
  }

  private void calculateDominanceFrontiers() {
    calculateDominanceFrontier(cfg.getEntry());
  }
    
    
    // Algorithm from:
    // http://www.cs.utexas.edu/~pingali/CS380C/2010/papers/ssaCytron.pdf
   
      //for each X in a bottom-up traversal of the dominator tree do
        //DF(X) <- 0
        //for each Y (member of) Succ(X) do
   /*local */// if idom(Y) != X then DF(X) <- DF(X) U {Y}
        //end
        //for each Z (member of) Children(x) do
            //for each Y (member of) LIF’(Z) do
   /*up*/         //if idom(Y) # X then DF’(X) - llF’(X) U {Y}
            //end
         //end
      //end
  

  /**
   * The dominance frontier DF(X) of a CFG node X is the set of all CFG nodes
   * Y such that X dominates a predecessor of Y but does not strictly dominate Y
   */
  private void calculateDominanceFrontier(BasicBlock X) {
    for(BasicBlock child : successors.get(X)) {
      calculateDominanceFrontier(child);
    }
    
    // calculate DF_local(X)
    for(BasicBlock Y : cfg.getSuccessors(X)) {
      if(getImmediateDominator(Y) != X) {
        dominanceFrontier.put(X, Y);
      }
    }
    // calculate 
    for(BasicBlock Z : successors.get(X)) {
      for(BasicBlock Y : dominanceFrontier.get(Z)) {
        if(getImmediateDominator(Y) != X) {
          dominanceFrontier.put(X, Y);
        }
      }
    }
  }

  /**
   * The dominance frontier of a basic block {@code n} is the set of all blocks that are immediate successors dominated by
   * {@code n}, but which aren't themselves strictly dominated by {@code n}.
   *
   */
  public Collection<BasicBlock> getFrontier(BasicBlock bb) {
    return dominanceFrontier.get(bb);
  }

  public Collection<BasicBlock> getChildren(BasicBlock x) {
    return successors.get(x);
  }
  

  public void dumpGraph() {
    DebugGraph dump = new DebugGraph("dominance");
    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      for (BasicBlock dominated : successors.get(basicBlock)) {
        dump.printEdge(basicBlock.getDebugId(), dominated.getDebugId());
      }
    }
    dump.close();
  }
}
