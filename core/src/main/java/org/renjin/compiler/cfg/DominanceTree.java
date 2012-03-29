package org.renjin.compiler.cfg;


import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class DominanceTree {

  private final ControlFlowGraph cfg;
  private final HashMultimap<BasicBlock, BasicBlock> Dom = HashMultimap.create();
  private final Graph<BasicBlock, DominanceEdge> tree = new DirectedSparseGraph<BasicBlock, DominanceEdge>();
  private final Multimap<BasicBlock, BasicBlock> dominanceFrontier = HashMultimap.create();
  
  public DominanceTree(ControlFlowGraph cfg) {
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
    for(BasicBlock n : filter(cfg.getLiveBasicBlocks(), not(equalTo(cfg.getEntry())))) {
      Dom.putAll(n, cfg.getLiveBasicBlocks());
    }
    
    // iteratively eliminate nodes that are not dominators
    boolean changes;
    do { 
      changes = false;
      for(BasicBlock n : filter(cfg.getLiveBasicBlocks(), not(equalTo(cfg.getEntry())))) {
          // Dom(n) = {n} union with intersection over all p in pred(n) of Dom(p)
          Set<BasicBlock> newDom = 
              Sets.union(Collections.singleton(n),
                  intersection(Iterables.transform(cfg.getGraph().getPredecessors(n), 
                      new Function<BasicBlock, Set<BasicBlock>>() {

                        @Override
                        public Set<BasicBlock> apply(BasicBlock input) {
                          return Dom.get(input);
                        }
                  })));
          
          Set<BasicBlock> original = Dom.get(n);
         
          if(!original.equals(newDom)) {
            Dom.replaceValues(n, newDom);
            changes = true;
          }
      }   
    } while(changes);
  }
  
  private void buildTree() {
    for(BasicBlock n : cfg.getBasicBlocks()) {
      BasicBlock d = calculateImmediateDominator(n);
      if(d != null) {
        tree.addEdge(new DominanceEdge(), d, n, EdgeType.DIRECTED);
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
    Collection<BasicBlock> parent = tree.getPredecessors(n);
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
  private boolean strictlyDominates(BasicBlock d, BasicBlock n) {
    return !d.equals(n) && dominates(d, n);
  }
  
  /**
   * A node d dominates a node n if every path 
   * from the start node to n must go through d. 
   * By definition, every node dominates itself.
   */
  private boolean dominates(BasicBlock d, BasicBlock n) {
    return Dom.containsEntry(n, d);
  }
  
  private Set<BasicBlock> dominators(BasicBlock n) {
    return Dom.get(n);
  }
  
  private Iterable<BasicBlock> strictDominators(BasicBlock n) {
    return Iterables.filter(Dom.get(n), not(equalTo(n)));
  }
  
  private Set<BasicBlock> intersection(Iterable<Set<BasicBlock>> sets) {
    Set<BasicBlock> intersection = null;
    for(Set<BasicBlock> set : sets) {
      if(intersection == null) {
        intersection = set;
      } else {
        intersection = Sets.intersection(intersection, set);
      }
    }
    return intersection == null ? Collections.<BasicBlock>emptySet() : intersection;
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
   * Y such that X dominates a predecessor of Y but does not strictly dominate
   * Y
   */
  private void calculateDominanceFrontier(BasicBlock X) {
    for(BasicBlock child : tree.getSuccessors(X)) {
      calculateDominanceFrontier(child);
    }
    
    // calculate DF_local(X)
    for(BasicBlock Y : cfg.getGraph().getSuccessors(X)) {
      if(getImmediateDominator(Y) != X) {
        dominanceFrontier.put(X, Y);
      }
    }
    // calculate 
    for(BasicBlock Z : tree.getSuccessors(X)) {
      for(BasicBlock Y : dominanceFrontier.get(Z)) {
        if(getImmediateDominator(Y) != X) {
          dominanceFrontier.put(X, Y);
        }
      }
    }
  }
  
  public Collection<BasicBlock> getFrontier(BasicBlock bb) {
    return dominanceFrontier.get(bb);
  }

  public Collection<BasicBlock> getChildren(BasicBlock x) {
    return tree.getSuccessors(x);
  }
  
  @Override
  public String toString() {
    return tree.toString();
  }
}
