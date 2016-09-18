package org.renjin.compiler.pipeline.node;

import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.repackaged.asm.Type;
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
  
  public DataNode(Vector vector) {
    super();
    if(vector instanceof MemoizedComputation) {
      this.vector = ((MemoizedComputation) vector).forceResult();
    } else {
      this.vector = vector;
    }
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
  public Vector getVector() {
    return vector;
  }

  @Override
  public NodeShape getShape() {
    return NodeShape.BOX;
  }

  @Override
  public Type getResultVectorType() {
    return Type.getType(vector.getClass());
  }

  @Override
  public boolean hasValue(double x) {
    return vector.length() == 1 && vector.getElementAsDouble(0) == x;
  }

  public boolean equivalent(DeferredNode other) {
    if(!(other instanceof DataNode)) {
      return false;
    }
    DataNode otherData = (DataNode) other;
    Vector.Type vectorType = this.getVector().getVectorType();
    if(!vectorType.equals(otherData.vector.getVectorType())) {
      return false;
    }
    if(this.vector.length() > 10) {
      return false;
    }
    for (int i = 0; i < this.vector.length(); i++) {
      if(vectorType.compareElements(this.vector, i, otherData.vector, i) != 0) {
        return false;
      }
    }
    return true;
  }

  @Override
  public DeferredNode call() {
    return this;
  }
}
