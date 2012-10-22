package org.renjin.appl;

import org.netlib.blas.BLAS;

public class ExternalRoutines {

  public static double dnrm2(int n, double[] x, int incx) {
    return BLAS.getInstance().dnrm2(n, x, incx);
  }
}
