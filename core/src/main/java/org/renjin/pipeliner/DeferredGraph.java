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
package org.renjin.pipeliner;

import org.renjin.pipeliner.fusion.FusedNode;
import org.renjin.pipeliner.fusion.LoopKernelCache;
import org.renjin.pipeliner.fusion.LoopKernels;
import org.renjin.pipeliner.node.*;
import org.renjin.pipeliner.optimize.Optimizers;
import org.renjin.primitives.ni.DeferredNativeCall;
import org.renjin.primitives.ni.NativeOutputVector;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.*;
import org.renjin.sexp.Vector;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Directed, acyclic graph (DAG) of a deferred computation.
 *
 * <p>This graph as is constructed at the moment that the
 * interpreter actually needs the result of a computation.
 */
public class DeferredGraph {

  private List<DeferredNode> rootNodes = new ArrayList<>();
  private List<DeferredNode> nodes = Lists.newArrayList();
  private IdentityHashMap<Vector, DeferredNode> vectorMap = Maps.newIdentityHashMap();
  private IdentityHashMap<DeferredNativeCall, CallNode> callMap = Maps.newIdentityHashMap();
  private Multimap<String, FunctionNode> computationIndex = HashMultimap.create();

  public DeferredGraph(DeferredNativeCall call) {
    addRoot(call);
  }

  public DeferredGraph(Vector root) {
    addRoot(root);
  }

  public DeferredGraph() {
  }

  public void optimize(LoopKernelCache loopKernelCache) {
    Optimizers optimizers = new Optimizers();
    optimizers.optimize(this);
    fuse(loopKernelCache);
  }
  
  public void fuse(LoopKernelCache loopKernelCache) {
    // Conduct a depth-first search of summary operators we can collapse

    Set<DeferredNode> visited = Sets.newIdentityHashSet();
    List<DeferredNode> toCheck = new ArrayList<>(rootNodes);
    for (DeferredNode rootNode : toCheck) {
      fuse(loopKernelCache, visited, rootNode);
    }
  }

  private void fuse(LoopKernelCache loopKernelCache, Set<DeferredNode> visited, DeferredNode node) {
    if(visited.add(node)) {
      // First time we've seen this node, try to fuse its operands
      // before trying itself
      for (DeferredNode operand : node.getOperands()) {
        fuse(loopKernelCache, visited, operand);
      }
    }
    FusedNode fused = tryFuse(node);
    if(fused != null) {
      fused.startCompilation(loopKernelCache);
      replaceNode(node, fused);
    }
  }

  private FusedNode tryFuse(DeferredNode root) {
    if(LoopKernels.INSTANCE.supports(root)) {
      return new FusedNode((FunctionNode) root);
    }
    return null;
  }

  void addRoot(Vector root) {
    DeferredNode rootNode = addNode(root);
    rootNodes.add(rootNode);
  }

  /**
   * Adds a new node to the graph, if a node does not already exist for the 
   * given {@code vector}
   */
  private DeferredNode addNode(Vector vector) {
    DeferredNode node = vectorMap.get(vector);
    if(node != null) {
      return node;
    }

    if(vector.isDeferred()) {
      if(vector instanceof NativeOutputVector) {
        // Add as the output of a native function call
        node = addOutputNode(vector);

      } else if(vector instanceof DeferredComputation) {
        node = addComputeNode((DeferredComputation) vector);
        
      } else {
        throw new UnsupportedOperationException("deferred: " + vector.getClass().getName());
      }
    } else {
      node = addDataNode(vector);
    }
    return node;
  }

  private DataNode addDataNode(Vector vector) {
    DataNode dataNode = new DataNode(vector);
    vectorMap.put(vector, dataNode);
    nodes.add(dataNode);
    return dataNode;
  }

  private DeferredNode addComputeNode(DeferredComputation vector) {

    // First find the nodes of all the children
    Vector[] operands = vector.getOperands();
    DeferredNode[] children = new DeferredNode[operands.length];
    for (int i = 0; i < operands.length; i++) {
      children[i] = addNode(operands[i]);
    }
    
    // Does the operation already exist in the graph?
    if(computationIndex.containsKey(vector.getComputationName())) {
      for (FunctionNode existingNode : computationIndex.get(vector.getComputationName())) {
        if(equivalent(children, existingNode.getOperands())) {
          return existingNode;
        }
      }
    }
    
    FunctionNode newNode = new FunctionNode(vector);
    newNode.addInputs(children);
    nodes.add(newNode);
    vectorMap.put(vector, newNode);
    computationIndex.put(vector.getComputationName(), newNode);
    return newNode;
  }

  private boolean equivalent(DeferredNode[] a, List<DeferredNode> b) {
    if(a.length != b.size()) {
      return false;
    }
    for (int i = 0; i < a.length; i++) {
      if(!equivalent(a[i], b.get(i))) {
        return false;
      }
    }
    return true;
  }

  private boolean equivalent(DeferredNode a, DeferredNode b) {
    if(a == b) {
      return true;
    }
    if(a instanceof DataNode) {
      return ((DataNode) a).equivalent(b);
    }
    return false;
  }

  private DeferredNode addOutputNode(Vector vector) {
    DeferredNode node;
    node = new OutputNode((NativeOutputVector) vector);
    vectorMap.put(vector, node);
    nodes.add(node);
    addCallChild(node, ((NativeOutputVector) vector).getCall());
    return node;
  }

  private CallNode addNode(DeferredNativeCall call) {
    CallNode node = callMap.get(call);
    if(node != null) {
      return node;
    }
    
    node = new CallNode(call);
    nodes.add(node);
    callMap.put(call, node);
    
    addChildren(node, call.getOperands());
    
    return node;
  }

  private void addCallChild(DeferredNode parentNode, DeferredNativeCall call) {

    CallNode callNode = addNode(call);
    
    parentNode.addInput(callNode);
    callNode.addOutput(parentNode);
  }

  private void addRoot(DeferredNativeCall call) {
    DeferredNode rootNode = new CallNode(call);
    rootNodes.add(rootNode);
    nodes.add(rootNode);
    addChildren(rootNode, call.getOperands());
  }

  private void addChildren(DeferredNode parent, Vector[] operands) {
    for(Vector operand : operands) {
      DeferredNode node = addNode(operand);
      parent.addInput(node);
      node.addOutput(parent);
    }
  }

  public void dumpGraph() {
    try {
      File tempFile = File.createTempFile("deferred", ".dot");
      PrintWriter writer = new PrintWriter(tempFile);
      printGraph(writer);
      writer.close();
      System.out.println("Dumping compute graph to " + tempFile.getAbsolutePath());
    } catch (IOException e) {
    }
  }

  public void printGraph(PrintWriter writer) {
    
    // Only list the nodes reacheable from roots
    Set<DeferredNode> nodes = Sets.newIdentityHashSet();
    ArrayDeque<DeferredNode> workingList = new ArrayDeque<>(rootNodes);
    while(!workingList.isEmpty()) {
      DeferredNode node = workingList.poll();
      if(nodes.add(node)) {
        workingList.addAll(node.getOperands());
      }
    }
    
    writer.println("digraph G {");
    printEdges(writer, nodes);
    printNodes(writer, nodes);
    writer.println("}");
    writer.flush();
  }

  private void printEdges(PrintWriter writer, Set<DeferredNode> nodes) {
    for(DeferredNode node : nodes) {
      for(DeferredNode operand : node.getOperands()) {
        writer.println(operand.getDebugId() + " -> " + node.getDebugId());
      }
    }
  }
  

  private void printNodes(PrintWriter writer, Set<DeferredNode> nodes) {
    for(DeferredNode node : nodes) {
      writer.println(node.getDebugId() + " [ label=\"" + node.getDebugLabel() + "\",  " +
          "shape=\"" + node.getShape().name().toLowerCase() + "\"]");
    }
  }

  public List<DeferredNode> getRoots() {
    return rootNodes;
  }

  public Vector getRootResult(int rootIndex) {
    return rootNodes.get(rootIndex).getVector();
  }

  public DeferredNode getRoot() {
    Preconditions.checkState(rootNodes.size() == 1);
    return rootNodes.get(0);
  }

  public List<DeferredNode> getNodes() {
    return nodes;
  }

  public void replaceNode(DeferredNode toReplace, DeferredNode replacementNode) {
    nodes.remove(toReplace);
    if(!nodes.contains(replacementNode)) {
      nodes.add(replacementNode);
    }
    
    if(rootNodes.remove(toReplace)) {
      rootNodes.add(replacementNode);
    }

    for(DeferredNode operand : toReplace.getOperands()) {
      operand.removeUse(toReplace);
    }

    for(DeferredNode node : toReplace.getUses()) {
      node.replaceOperand(toReplace, replacementNode);
    }
  }
}
