package org.renjin.compiler.pipeline.node;

import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Vector;

/**
 * A node in the graph that is already computed, for example, 
 * a DoubleArrayVector.
 */
public class DataNode extends DeferredNode {

  private Vector vector;
  
  public DataNode(int id, Vector vector) {
    super(id);
    this.vector = vector;
  }

  @Override
  public String getDebugLabel() {
    if(vector.length() == 1) {
      if(vector.isElementNA(0)) {
        return "NA";
      }
      if(vector instanceof IntVector) {
        return vector.getElementAsInt(0) + "L";
      }
      if(vector instanceof DoubleVector) {
        return Double.toString(vector.getElementAsDouble(0));
      } 
      if(vector instanceof LogicalVector) {
        return vector.getElementAsRawLogical(0) == 0 ? "F" : "T";
      }
    } 
    return "[" + vector.length() + "]";
  }

  @Override
  public String getShape() {
    return "box";
  }

  @Override
  public boolean hasValue(double x) {
    return vector.length() == 1 && vector.getElementAsDouble(0) == x;
  }

  @Override
  public boolean equivalent(DeferredNode that) {

    if (!(that instanceof DataNode)) {
      return false;
    }

    DataNode thatNode = (DataNode) that;
    if (this.vector.length() != thatNode.vector.length()) {
      return false;
    }
    if (vector.length() > 10) {
      return false;
    }
    for (int i = 0; i != vector.length(); ++i) {
      if (vector.getVectorType().compareElements(vector, i, thatNode.vector, i) != 0) {
        return false;
      }
    }
    return true;
  }
}
