package org.renjin.primitives.summary;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.VectorPipeliner;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.*;

public class DeferredMean extends DeferredSummary {

  public DeferredMean(Vector vector, AttributeMap attributes) {
    super(vector, attributes);
  }

  @Override
  public String getComputationName() {
    return "mean";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new DeferredMean(vector, attributes);
  }

  protected double calculate() {
    double sum = 0;
    for(int i=0;i!=vector.length();++i) {
      sum += vector.getElementAsDouble(i);
    }
    return sum / vector.length();
  }
}
