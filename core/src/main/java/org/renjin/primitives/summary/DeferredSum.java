package org.renjin.primitives.summary;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class DeferredSum extends DeferredSummary {

  public DeferredSum(Vector vector, AttributeMap attributes) {
    super(vector, attributes);
  }

  @Override
  protected double calculate() {
    double sum = 0;
    System.err.println("EEK serially calculation - sum");
    for(int i=0;i!=vector.length();++i) {
      sum += vector.getElementAsDouble(i);
    }
    return sum;
  }


  @Override
  public String getComputationName() {
    return "sum";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new DeferredSum(vector, attributes);
  }
}
