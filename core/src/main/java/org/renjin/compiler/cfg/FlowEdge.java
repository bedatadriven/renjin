package org.renjin.compiler.cfg;

/**
 * Control flow edge from one basic block to another.
 */
public class FlowEdge {
  private final BasicBlock predecessor;
  private final BasicBlock successor;
  
  private boolean executable = false;

  public FlowEdge(BasicBlock predecessor, BasicBlock successor) {
    this.predecessor = predecessor;
    this.successor = successor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    FlowEdge flowEdge = (FlowEdge) o;
    return flowEdge.predecessor == this.predecessor &&
           flowEdge.successor == this.successor;
  }

  public BasicBlock getPredecessor() {
    return predecessor;
  }

  public BasicBlock getSuccessor() {
    return successor;
  }

  public boolean isExecutable() {
    return executable;
  }

  public void setExecutable(boolean executable) {
    this.executable = executable;
  }

  @Override
  public int hashCode() {
    int result = predecessor.hashCode();
    result = 31 * result + successor.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return getPredecessor().getDebugId() + " -> " + getSuccessor().getDebugId();
  }
}
