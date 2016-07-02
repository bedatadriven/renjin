package org.renjin.compiler.cfg;

/**
 * Edge between two basic blocks in the Control Flow Graph
 */
public class Edge {
  private BasicBlock predecessor;
  private BasicBlock successor;

  public Edge(BasicBlock predecessor, BasicBlock successor) {
    this.predecessor = predecessor;
    this.successor = successor;
  }

  public BasicBlock getPredecessor() {
    return predecessor;
  }

  public BasicBlock getSuccessor() {
    return successor;
  }
}
