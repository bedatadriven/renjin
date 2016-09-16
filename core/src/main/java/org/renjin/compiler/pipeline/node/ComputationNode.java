package org.renjin.compiler.pipeline.node;

import org.renjin.compiler.pipeline.specialization.SpecializationKey;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.Vector;

public class ComputationNode extends DeferredNode {

  private DeferredComputation vector;

  public ComputationNode(DeferredComputation vector) {
    super();
    this.vector = vector;
  }

  public void replaceVector(DeferredComputation vector) {
    this.vector = vector;
  }

//  public boolean hasValue(double v) {
//    return (vector instanceof DoubleArrayVector || vector instanceof IntArrayVector) &&
//        vector.length() == 1 &&
//        vector.getElementAsDouble(0) == v;
//  }

  @Override
  public String getDebugLabel() {
    return vector.getComputationName();
  }

  @Override
  public Vector getVector() {
    return vector;
  }

  @Override
  public NodeShape getShape() {
    if(vector instanceof MemoizedComputation) {
      return NodeShape.ELLIPSE;
    } else {
      return NodeShape.PARALLELOGRAM;
    }
  }


  public SpecializationKey jitKey() {
//    List<DeferredNode> nodes = flatten();
//    Class[] classes = new Class[nodes.size()];
//    for(int i=0;i!=classes.length;++i) {
//      classes[i] = nodes.get(i).getVector().getClass();
//    }
//    return new SpecializationKey(classes);
    throw new UnsupportedOperationException("TODO");
  }


  public String getComputationName() {
    return vector.getComputationName();
  }

}
