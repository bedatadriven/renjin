package org.renjin.primitives.vector;

import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


public class LogicalBinaryVector extends LogicalVector {

  public interface Function {
    int apply(Vector x, Vector y, int index);
  }

  private final Vector x;
  private final Vector y;
  private final Function function;
  private final int length;

  public LogicalBinaryVector(Vector x, Vector y, Function function) {
    this.x = x;
    this.y = y;
    this.function = function;
    this.length = Math.max(x.length(), y.length());
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return function.apply(x, y, index);
  }

  @Override
  protected SEXP cloneWithNewAttributes(PairList attributes) {
    throw new UnsupportedOperationException();
  }
}
