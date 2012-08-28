package org.renjin.primitives.vector;


import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;

/**
 * An {@code IntVector} which computes its elements as requested.
 */
public class ComputingIntVector extends IntVector {

  public interface Functor {
    int apply(int index);
  }

  private Functor functor;
  private int length;

  public ComputingIntVector(Functor functor, int length, AttributeMap attributes) {
    super(attributes);
    this.functor = functor;
    this.length = length;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsInt(int i) {
    return functor.apply(i);
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ComputingIntVector(functor, length, attributes);
  }
}
