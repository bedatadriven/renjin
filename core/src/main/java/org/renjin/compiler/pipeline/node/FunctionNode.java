package org.renjin.compiler.pipeline.node;

import org.renjin.compiler.pipeline.specialization.SpecializationKey;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.*;

/**
 * Node that applies a function to one or more vector operands.
 */
public class FunctionNode extends DeferredNode implements Runnable {

  private DeferredComputation vector;
  private Vector result;

  public FunctionNode(DeferredComputation vector) {
    super();
    this.vector = vector;
  }

  public void replaceVector(DeferredComputation vector) {
    this.vector = vector;
  }

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
  public void run() {
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
  }
}
