package org.renjin.compiler.pipeline.node;

import org.renjin.compiler.pipeline.specialization.SpecializationKey;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.*;

public class ComputationNode extends DeferredNode {

  private DeferredComputation vector;
  private Vector result;

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

  @Override
  public Type getResultVectorType() {
    if(vector instanceof DoubleVector) {
      return Type.getType(DoubleArrayVector.class);
    } else if(vector instanceof IntArrayVector) {
      return Type.getType(IntArrayVector.class);
    } else if(vector instanceof LogicalVector) {
      return Type.getType(LogicalArrayVector.class);
    } else {
      throw new UnsupportedOperationException("TODO: " + vector.getClass().getName());
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

  @Override
  public DeferredNode call() {
    if(vector instanceof MemoizedComputation) {
      this.result = ((MemoizedComputation) vector).forceResult();
    } else if(vector instanceof DoubleVector) {
      this.result = DoubleArrayVector.unsafe(vector.toDoubleArray());
    } else if(vector instanceof IntVector) {
      this.result = IntArrayVector.unsafe(vector.toIntArray());
    } else if(vector instanceof LogicalVector) {
      this.result = LogicalArrayVector.unsafe(vector.toIntArray());
    } else {
      throw new UnsupportedOperationException("vector: " + vector.getClass().getName());
    }
    return this;
  }
}
