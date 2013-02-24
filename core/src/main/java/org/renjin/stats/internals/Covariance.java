package org.renjin.stats.internals;



import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Vector;

public class Covariance {

  @Primitive
  public static DoubleVector cor(AtomicVector x, AtomicVector y, int naMethod, boolean kendall) {

    if(kendall) {
      throw new EvalException("kendall=true nyi");
    }

    return new VarianceCalculator(x, y, naMethod)
    .withPearsonCorrelation()
    .calculate();
  }


  @Primitive
  public static Vector cov(AtomicVector x, AtomicVector y, int naMethod, boolean kendall) {
    if(kendall) {
      throw new EvalException("kendall=true nyi");
    }

    return new VarianceCalculator(x, y, naMethod)
    .withCovarianceMethod()
    .calculate();
  }

}
