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
package org.renjin.gcc.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Identifies uses of uninitialized variables
 */
public class DataFlowAnalysis<T> {

  private ControlFlowGraph cfg;
  private FlowFunction<T> flowFunction;
  
  private Map<ControlFlowGraph.Node, T> entryState = new HashMap<>();
  private Map<ControlFlowGraph.Node, T> exitState = new HashMap<>();
  
  public DataFlowAnalysis(ControlFlowGraph cfg, FlowFunction<T> flowFunction) {
    this.cfg = cfg;
    this.flowFunction = flowFunction;
  }

  /**
   * Solves the dataflow equation at the boundaries of the basic blocks
   */
  public void solve() {
    
    // Step 1: initialize the state of all nodes
    T initialState = flowFunction.initialState();

    for (ControlFlowGraph.Node node : cfg.getNodes()) {
      entryState.put(node, initialState);
      exitState.put(node, flowFunction.transfer(initialState, node.getBasicBlock()));
    }
    
    // While (sets are still changing)
    boolean changed;
    do {
      changed = false;
      
      for (ControlFlowGraph.Node node : cfg.getBasicBlockNodes()) {
        // recalculate the node's input
        T currentEntryState = entryState.get(node);
        T updatedEntryState = applyJoin(node);

        if (!updatedEntryState.equals(currentEntryState)) {
          entryState.put(node, updatedEntryState);

          T currentExitState = exitState.get(node);
          T updatedExitState = flowFunction.transfer(updatedEntryState, node.getBasicBlock());
          if (!currentExitState.equals(updatedExitState)) {
            changed = true;
            exitState.put(node, updatedExitState);
          }
        }
      }
    } while(changed);
  }

  /**
   * Updates a node's entry state as a function of all the incoming node's states.
   */
  private T applyJoin(ControlFlowGraph.Node node) {

    List<T> inputs = new ArrayList<>();
    for (ControlFlowGraph.Node incoming : node.getIncoming()) {
      inputs.add(exitState.get(incoming));
    }

    return flowFunction.join(inputs);
  }

  public T getEntryState(ControlFlowGraph.Node node) {
    T state = entryState.get(node);
    if(state == null) {
      throw new IllegalArgumentException("Node: " + node);
    }
    return state;
  }

  public void dump() {
    for (ControlFlowGraph.Node node : cfg.getBasicBlockNodes()) {
      System.out.println(node.getId() + ": " +
          entryState.get(node) + " -> " +
          exitState.get(node));
    }
  }

}
