package r.compiler.cfg;

public class Edge {
  private boolean backEdge;

  public Edge(boolean backEdge) {
    super();
    this.backEdge = backEdge;
  }

  public boolean isBackEdge() {
    return backEdge;
  }
  
}
