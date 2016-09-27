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

import org.renjin.compiler.pipeline.optimize.Optimizers;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
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
      String shape = "box";
      if(node.isComputation()) {
        if(node.getComputation() instanceof  MemoizedComputation) {
          shape = "ellipse";
        } else {
          shape = "parallelogram";
        }
      }
      writer.println(node.getDebugId() + " [ label=\"" + node.getDebugLabel() + "\",  " +
          "shape=\"" + shape + "\"]");
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

}
