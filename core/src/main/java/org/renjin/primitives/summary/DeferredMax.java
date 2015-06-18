package org.renjin.primitives.summary;

import org.renjin.primitives.Summary.RangeCalculator;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class DeferredMax extends DeferredSummary {

  public DeferredMax(Vector vector, AttributeMap attributes) {
    super(vector, attributes);
  }

  @Override
  protected double calculate() {
    try {
      return new RangeCalculator().addList(new ListVector(vector))
          .setRemoveNA(false).getMaximum().getElementAsDouble(0);
    } catch (Exception e) {
      return DoubleVector.NA;
    }
  }

  @Override
  public String getComputationName() {
    return "max";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new DeferredMax(vector, attributes);
  }
}
