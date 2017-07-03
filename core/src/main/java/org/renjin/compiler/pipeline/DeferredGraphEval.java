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
package org.renjin.compiler.pipeline;

import org.renjin.compiler.pipeline.node.DataNode;
import org.renjin.compiler.pipeline.node.DeferredNode;
import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DeferredGraphEval {
  
  private DeferredGraph graph;
  private ExecutorCompletionService<DeferredNode> service;

  /**
   * Set of nodes that have already been visited by the {@code scheduleInputs} routine.
   */
  private Set<DeferredNode> scheduled = Sets.newIdentityHashSet();

  /**
   * Set of nodes that have been submitted to the {@code ExecutionCompletionService}.
   */
  private Map<DeferredNode, Future<DeferredNode>> submitted = Maps.newIdentityHashMap();

  /**
   * Count of nodes that have been submitted for computation but not yet completed.
   */
  private int pendingCount = 0;
  
  public DeferredGraphEval(DeferredGraph graph, ExecutorService service) {
    this.graph = graph;
    this.service = new ExecutorCompletionService<DeferredNode>(service);
  }
  
  public void execute() {
    scheduleRoots();
    while(pendingCount > 0) {
      DeferredNode completed = nextCompleted();
      submitDependents(completed);
    }
  }

  private DeferredNode nextCompleted() {
    DeferredNode completed;
    try {
      completed = service.take().get();
      pendingCount--;
    } catch (InterruptedException e) {
      throw new EvalException("Deferred vector execution interrupted.");
    } catch (ExecutionException e) {
      throw new EvalException(e.getCause());
    }
    return completed;
  }

  private void submitDependents(DeferredNode completed) {
    for (DeferredNode dependentNode : completed.getUses()) {
      if(!submitted.containsKey(dependentNode) && 
          inputsComplete(dependentNode)) {
        
        if(dependentNode instanceof Runnable) {
          submit(dependentNode);
        } else {
          submitDependents(dependentNode);
        }
      }
    }
  }

  private void scheduleRoots() {
    for (DeferredNode node : graph.getRoots()) {
      if(node instanceof Runnable) {
        schedule(node);
      }
    }
  }

  private void schedule(DeferredNode node) {
    
    boolean ready = true;
    
    for (DeferredNode input : node.getOperands()) {
      if(needsComputing(input)) {
        schedule(input);
        ready = false;
      }
    }
    
    if(ready) {
      submit(node);
    }
  }

  private boolean needsComputing(DeferredNode input) {
    return !(input instanceof DataNode);
  }

  private boolean inputsComplete(DeferredNode node) {
    for (DeferredNode input : node.getOperands()) {
      if(input instanceof Runnable) {
        Future<DeferredNode> future = submitted.get(input);
        if(future == null || !future.isDone()) {
          return false;
        }
      }
    }
    return true;
  }

  private void submit(DeferredNode node) {
    Future<DeferredNode> future = service.submit((Runnable) node, node);
    submitted.put(node, future);
    pendingCount ++;
  }

}
