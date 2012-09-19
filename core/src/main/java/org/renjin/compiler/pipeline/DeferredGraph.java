package org.renjin.compiler.pipeline;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.compiler.pipeline.optimize.Optimizers;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.Vector;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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

  public static boolean DEBUG = false;
  public static final int JIT_THRESHOLD = 1000;

  private DeferredNode rootNode;
  private List<DeferredNode> nodes = Lists.newArrayList();
  private int nextNodeId = 1;
  private IdentityHashMap<Vector, DeferredNode> nodeMap = Maps.newIdentityHashMap();

  public DeferredGraph(DeferredComputation root) {
    this.rootNode = new DeferredNode(nextNodeId(), root);
    nodes.add(rootNode);
    nodeMap.put(root, rootNode);
    addChildren(this.rootNode);

    Optimizers optimizers = new Optimizers();
    optimizers.optimize(this);
    removeOrphans();
  }

  private int nextNodeId() {
    return nextNodeId++;
  }

  private void addChildren(DeferredNode parent) {
    for(Vector operand : parent.getComputation().getOperands()) {
      DeferredNode node = nodeMap.get(operand);
      if(node == null) {
        node = new DeferredNode(nextNodeId(), operand);
        if(node.isComputation()) {
          addChildren(node);
        }
        node = tryMerge(node);
        nodeMap.put(operand, node);
      }
      parent.addOperand(node);
      node.addUse(parent);
    }
  }

  private DeferredNode tryMerge(DeferredNode newNode) {
    for(DeferredNode node : nodeMap.values()) {
      if(node.equivalent(newNode)) {
        return node;
      }
    }
    nodes.add(newNode);
    return newNode;
  }

  private void dumpGraph() {
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
      writer.println(node.getDebugId() + " [ label=\"" + node.getDebugLabel() + "\"]");
    }
  }

  public DeferredNode getRoot() {
    return rootNode;
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
        if(node != rootNode && !node.isUsed()) {
          removing = true;
          it.remove();
        }
      }
    } while(removing);
  }

  public Vector compute() {
    if(DEBUG) {
      dumpGraph();
    }
    return compute(rootNode);
  }

  private Vector compute(DeferredNode node) {
    // depth first compute
    for(DeferredNode operand : node.getOperands()) {
      compute(operand);
    }
    if(node.isComputation()) {
      // TODO: at the moment, we can compile only a small number of summary
      // function, eventually we want to generate bytecode on the fly based
      // on their implementations elsewhere.
      if(node.getComputation().getComputationName().equals("mean") ||
         node.getComputation().getComputationName().equals("rowMeans")) {
        try {

          if(DEBUG) {
            System.out.println("Computing " + node);
          }
          Vector[] operands = node.flattenVectors();
          JittedComputation computer = DeferredJitCache.INSTANCE.compile(node);

          long start = System.nanoTime();

          Vector result = DoubleArrayVector.unsafe(computer.compute(operands));

          long time = System.nanoTime() - start;
          if(DEBUG) {
            System.out.println("compute: " + (time/1e6) + "ms");
          }

          ((MemoizedComputation)node.getVector()).setResult(result);
          node.setResult(result);
        } catch(Throwable e) {
          throw new RuntimeException("Exception compiling node " + node, e);
        }
      } else if(node.getVector() instanceof MemoizedComputation) {
        node.setResult(((MemoizedComputation) node.getVector()).forceResult());
      }
    }
    return node.getVector();
  }
}
