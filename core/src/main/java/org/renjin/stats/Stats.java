package org.renjin.stats;

import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.ListVector;

public class Stats {


  public static ListVector massdist(DoubleVector x, DoubleVector weights,
      int nx, double xlo, double xhi, DoubleVector y, int ny) {
    
    ListVector.NamedBuilder result = new ListVector.NamedBuilder();
    result.add("y", 
        new DoubleVector(Massdist.massdist(x.toDoubleArray(), weights.toDoubleArray(), nx, xlo, xhi, y.toDoubleArray(), ny)));
    
    return result.build();
    
  }
 
  
  public static ListVector R_approx(
      DoubleVector x, 
      DoubleVector y, 
      int nx, 
      DoubleVector xoutVector,
      int nout,
      int method,
      double yleft, 
      double yright, 
      double f) {
    
    double xout[] = xoutVector.toDoubleArray();
    Approx.R_approx(x.toDoubleArray(), y.toDoubleArray(), nx, xout, nout, method,
        yleft, yright, f);
    
    ListVector.NamedBuilder result = new ListVector.NamedBuilder();
    result.add("xout", new DoubleVector(xout));
    return result.build();
  }
}