package org.renjin.compiler.cfg;

public class Edge {
  private boolean backEdge;

  public Edge(boolean backEdge) {
    super();
    this.backEdge = backEdge;
  }

  public boolean isBackEdge() {
    return backEdge;
  }
  
  @Override
  public String toString() {
    return backEdge ? "backEdge" : "edge";
  }
  
}
