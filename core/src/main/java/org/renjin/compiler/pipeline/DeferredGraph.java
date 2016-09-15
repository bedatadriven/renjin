package org.renjin.compiler.pipeline;

import org.renjin.compiler.pipeline.node.*;
import org.renjin.compiler.pipeline.optimize.Optimizers;
import org.renjin.primitives.ni.DeferredNativeCall;
import org.renjin.primitives.ni.NativeOutputVector;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.Vector;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * Directed, acyclic graph (DAG) of a deferred computation.
 *
 * <p>This graph as is constructed at the moment that the
 * interpreter actually needs the result of a computation.
 *
 */
public class DeferredGraph {

  private List<DeferredNode> rootNodes = new ArrayList<>();
  private List<DeferredNode> nodes = Lists.newArrayList();
  private int nextNodeId = 1;
  private IdentityHashMap<Vector, DeferredNode> vectorMap = Maps.newIdentityHashMap();
  private IdentityHashMap<DeferredNativeCall, CallNode> callMap = Maps.newIdentityHashMap();

  public DeferredGraph(DeferredNativeCall call) {
    addRoot(call);
  }

  public DeferredGraph(Vector root) {
    addRoot(root);
  }

  public void optimize() {
    Optimizers optimizers = new Optimizers();
    optimizers.optimize(this);
    removeOrphans();
  }
  

  private void addRoot(Vector root) {
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
        node = new OutputNode(nextNodeId(), (NativeOutputVector) vector);
      } else if(vector instanceof DeferredComputation) {
        node = new ComputationNode(nextNodeId(), (DeferredComputation) vector);
      } else {
        throw new UnsupportedOperationException("deferred: " + vector.getClass().getName());
      }
    } else {
      node = new DataNode(nextNodeId(), vector);
    }
    
    nodes.add(node);
    vectorMap.put(vector, node);

    if(vector instanceof NativeOutputVector) {
      addCallChild(node, ((NativeOutputVector) vector).getCall());

    } else if(vector instanceof DeferredComputation) {
      addChildren(node, ((DeferredComputation) vector).getOperands());
    }
    
    return node;
  }
  
  private CallNode addNode(DeferredNativeCall call) {
    CallNode node = callMap.get(call);
    if(node != null) {
      return node;
    }
    
    node = new CallNode(nextNodeId(), call);
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
    DeferredNode rootNode = new CallNode(nextNodeId(), call);
    rootNodes.add(rootNode);
    nodes.add(rootNode);
    addChildren(rootNode, call.getOperands());
  }

  private int nextNodeId() {
    return nextNodeId++;
  }

  private void addChildren(DeferredNode parent, Vector[] operands) {
    for(Vector operand : operands) {
      DeferredNode node = addNode(operand);
      parent.addInput(node);
      node.addOutput(parent);
    }
  }

  private DeferredNode tryMerge(DeferredNode newNode) {
    for(DeferredNode node : vectorMap.values()) {
      if(node.equivalent(newNode)) {
        return node;
      }
    }
    nodes.add(newNode);
    return newNode;
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
    writer.println("digraph G {");
    printEdges(writer);
    printNodes(writer);
    writer.println("}");
    writer.flush();
  }

  private void printEdges(PrintWriter writer) {
    for(DeferredNode node : nodes) {
      for(DeferredNode operand : node.getOperands()) {
        writer.println(node.getDebugId() + " -> " + operand.getDebugId());
      }
    }
  }
  

  private void printNodes(PrintWriter writer) {
    for(DeferredNode node : nodes) {
      writer.println(node.getDebugId() + " [ label=\"" + node.getDebugLabel() + "\",  " +
          "shape=\"" + node.getShape() + "\"]");
    }
  }

  public Iterable<DeferredNode> getRoots() {
    return rootNodes;
  }

  public DeferredNode getRoot() {
    Preconditions.checkState(rootNodes.size() == 1);
    return rootNodes.get(0);
  }
  

  public List<DeferredNode> getNodes() {
    return nodes;
  }

  public void replaceNode(DeferredNode toReplace, DeferredNode replacementValue) {
    nodes.remove(toReplace);
    if(!nodes.contains(replacementValue)) {
      nodes.add(replacementValue);
    }

    for(DeferredNode operand : toReplace.getOperands()) {
      operand.removeUse(toReplace);
    }

    for(DeferredNode node : nodes) {
      node.replaceOperand(toReplace, replacementValue);
      node.replaceUse(toReplace, replacementValue);
    }
  }

  private void removeOrphans() {
    boolean removing;
    do {
      removing = false;
      ListIterator<DeferredNode> it = nodes.listIterator();
      while(it.hasNext()) {
        DeferredNode node = it.next();
        if(!rootNodes.contains(node) && !node.isUsed()) {
          removing = true;
          it.remove();
        }
      }
    } while(removing);
  }

}
