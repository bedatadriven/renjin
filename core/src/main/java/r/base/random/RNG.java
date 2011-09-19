/*
 * unif_rand() produces different random numbers when compared to R
 * whatever the seed is. I think we can pass it until this gets a higher priorty.
 */
package r.base.random;

import org.apache.commons.math.random.MersenneTwister;
import r.jvmi.annotations.Primitive;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.exception.EvalException;

public class RNG {

  public static MersenneTwister mersenneTwisterAlg = null;
  public static RNGtype RNG_kind = RNGtype.MERSENNE_TWISTER; //default
  public static N01type N01_kind = N01type.INVERSION; //default
  static int[] dummy = new int[625];
  static DoubleVector seeds;
  static int randomseed = 0;
  static double i2_32m1 = 2.328306437080797e-10;/* = 1/(2^32 - 1) */

  static RNGTAB[] RNG_Table = new RNGTAB[]{
    new RNGTAB(RNGtype.WICHMANN_HILL, N01type.BUGGY_KINDERMAN_RAMAGE, "Wichmann-Hill"),
    new RNGTAB(RNGtype.MARSAGLIA_MULTICARRY, N01type.BUGGY_KINDERMAN_RAMAGE, "Marsaglia-MultiCarry"),
    new RNGTAB(RNGtype.SUPER_DUPER, N01type.BUGGY_KINDERMAN_RAMAGE, "Super-Duper"),
    new RNGTAB(RNGtype.MERSENNE_TWISTER, N01type.BUGGY_KINDERMAN_RAMAGE, "Mersenne-Twister"),
    new RNGTAB(RNGtype.KNUTH_TAOCP, N01type.BUGGY_KINDERMAN_RAMAGE, "Knuth-TAOCP"),
    new RNGTAB(RNGtype.USER_UNIF, N01type.BUGGY_KINDERMAN_RAMAGE, "User-supplied"),
    new RNGTAB(RNGtype.KNUTH_TAOCP2, N01type.BUGGY_KINDERMAN_RAMAGE, "Knuth-TAOCP-2002")};

  public static IntVector RNGkind(int kind, int normalkind) {
    try {
      RNG.RNG_kind = RNGtype.values()[kind];
    } catch (Exception e) {
      throw new EvalException("RNGkind: unimplemented RNG kind " + kind);
    }

    try {
      RNG.N01_kind = N01type.values()[normalkind];
    } catch (Exception e) {
      throw new EvalException("invalid Normal type in RNGkind");
    }

    RNG.RNG_kind = RNGtype.values()[kind];
    RNG.N01_kind = N01type.values()[normalkind];
    System.out.println("Random generator is set to " + RNG.RNG_kind + " and " + RNG.N01_kind);
    return (new IntVector(RNG.RNG_kind.ordinal(), RNG.N01_kind.ordinal()));
  }

  
  /*
   * Primitives.
   */
  @Primitive("set.seed")
  public static void set_seed(int seed, int kind, int normalkind) {
    RNG.randomseed = seed;
    RNG.RNGkind(kind, normalkind);
  }

  @Primitive("runif")
  public static DoubleVector runif(int n, double a, double b) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(a + unif_rand() * (b - a));
    }
    return (vb.build());
  }

  @Primitive("rnorm")
  public static DoubleVector rnorm(int n, double mean, double sd) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Normal.rnorm(mean, sd));
    }
    return (vb.build());
  }
  
  @Primitive("rgamma")
  public static DoubleVector rgamma(int n, double shape, double scale) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Gamma.rgamma(shape, scale));
    }
    return (vb.build());
  }
  
  @Primitive("rchisq")
  public static DoubleVector rchisq(int n, double df) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(ChiSquare.rchisq(df));
    }
    return (vb.build());
  }
  
  @Primitive("rnchisq")
  public static DoubleVector rnchisq(int n, double df, double ncp) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(ChiSquare.rnchisq(df, ncp));
    }
    return (vb.build());
  }
  
  
  
  public static double unif_rand() {
    double value;

    switch (RNG_kind) {

      case WICHMANN_HILL:
        throw new EvalException(RNG_kind + " not implemented yet");

      case MARSAGLIA_MULTICARRY:
        throw new EvalException(RNG_kind + " not implemented yet");

      case SUPER_DUPER:
        throw new EvalException(RNG_kind + " not implemented yet");

      case MERSENNE_TWISTER:
        if (mersenneTwisterAlg == null) {
          if (RNG.randomseed == 0) {
            Randomize(RNG_kind);
          }
          mersenneTwisterAlg = new MersenneTwister((long) RNG.randomseed);
        }
        return (mersenneTwisterAlg.nextDouble());

      case KNUTH_TAOCP:
      case KNUTH_TAOCP2:
        throw new EvalException(RNG_kind + " not implemented yet");
      case USER_UNIF:
        throw new EvalException(RNG_kind + " not implemented yet");
      default:
        throw new EvalException(RNG_kind + " not implemented yet");
    }
  }

  /*
   * This part of R is platform dependent. this formula is random itself :)
   */
  static void Randomize(RNGtype kind) {
    int sseed;
    sseed = (int) (new java.util.Date()).getTime();
    RNG.randomseed = sseed;
  }
}
