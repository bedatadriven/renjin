package org.renjin.graphics;

import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;


public enum ClippingMode {
  
  PLOT(Logical.FALSE),
  FIGURE(Logical.TRUE),
  DEVICE(Logical.NA);

  private final Logical logicalValue;

  ClippingMode(Logical logicalValue) {
    this.logicalValue = logicalValue;
  }

  public LogicalVector toExp() {
    return new LogicalVector(logicalValue);
  }

  public static ClippingMode fromExp(SEXP exp) {
    if(exp instanceof LogicalVector) {
      switch(((LogicalVector) exp).getElementAsLogical(0)) {
        case FALSE:
          return PLOT;
        case TRUE:
          return FIGURE;
        case NA:
          return DEVICE;
      }
    }
    throw new IllegalArgumentException(exp.toString());
  }
}
