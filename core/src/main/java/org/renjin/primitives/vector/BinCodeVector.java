package org.renjin.primitives.vector;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;


public class BinCodeVector extends IntVector {

  private Vector x;
  private double[] breaks;
  private boolean left;
  private boolean includeBorder;

  public BinCodeVector(Vector x, double breaks[], boolean left, boolean includeBorder,
                        AttributeMap attributes) {
    super(attributes);
    this.x = x;
    this.breaks = breaks;
    this.left = left;
    this.includeBorder = includeBorder;
  }

  @Override
  public int length() {
    return x.length();
  }

  @Override
  public int getElementAsInt(int i) {
    if(!x.isElementNA(i)) {
      int lo = 0;
      int hi = breaks.length - 1;
      double xi = x.getElementAsDouble(i);
      boolean outOfBounds =
              xi < breaks[lo] ||
                      breaks[hi] < xi ||
                      (xi == breaks[left ? hi : lo] && !includeBorder);

      if(!outOfBounds) {
        while(hi - lo >= 2) {
          int code = (hi + lo) / 2;
          if(xi > breaks[code] || (left && xi == breaks[code])) {
            lo = code;
          } else {
            hi = code;
          }
        }
        return lo+1;
      }
    }
    return IntVector.NA;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new BinCodeVector(x, breaks, left, includeBorder, attributes);
  }
}
