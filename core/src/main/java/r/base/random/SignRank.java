package r.base.random;

public class SignRank {

  public static double rsignrank(double n) {
    int i, k;
    double r;


    /* NaNs propagated correctly */
    if (Double.isNaN(n)) {
      return (n);
    }

    n = Math.floor(n + 0.5);
    if (n < 0) {
      return Double.NaN;
    }

    if (n == 0) {
      return (0);
    }

    r = 0.0;
    k = (int) n;
    for (i = 0; i < k;) {
      r += (++i) * Math.floor(RNG.unif_rand() + 0.5);
    }
    return (r);

  }
}
