package org.renjin.compiler.pipeline;

import org.renjin.compiler.pipeline.fusion.LoopKernels;
import org.renjin.compiler.pipeline.node.*;
import org.renjin.compiler.pipeline.optimize.Optimizers;
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
 *
 */
public class DeferredGraph {

  private List<DeferredNode> rootNodes = new ArrayList<>();
  private List<DeferredNode> nodes = Lists.newArrayList();
  private IdentityHashMap<Vector, DeferredNode> vectorMap = Maps.newIdentityHashMap();
  private IdentityHashMap<DeferredNativeCall, CallNode> callMap = Maps.newIdentityHashMap();
  private Multimap<String, ComputationNode> computationIndex = HashMultimap.create();

  public DeferredGraph(DeferredNativeCall call) {
    addRoot(call);
  }

  public DeferredGraph(Vector root) {
    addRoot(root);
  }

  public void optimize() {
    Optimizers optimizers = new Optimizers();
    optimizers.optimize(this);
    fuse();
  }
  
  public void fuse() {
    ArrayDeque<DeferredNode> workList = new ArrayDeque<>(rootNodes);
    while(!workList.isEmpty()) {
      DeferredNode node = workList.poll();
      DeferredNode fused = tryFuse(node);
      
      if(fused != null) {
        replaceNode(node, fused);
        workList.addAll(fused.getOperands());
      } else {
        workList.addAll(node.getOperands());
      }
    }
  }

  private DeferredNode tryFuse(DeferredNode root) {
    if(LoopKernels.INSTANCE.supports(root)) {
      return new FusedNode((ComputationNode) root);
    }
    return null;
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
      for (ComputationNode existingNode : computationIndex.get(vector.getComputationName())) {
        if(equivalent(children, existingNode.getOperands())) {
          return existingNode;
        }
      }
    }
    
    ComputationNode newNode = new ComputationNode(vector);
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
      node.replaceUse(toReplace, replacementNode);
    }
  }

}
