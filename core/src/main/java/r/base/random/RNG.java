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
    //System.out.println("Random generator is set to " + RNG.RNG_kind + " and " + RNG.N01_kind);
    return (new IntVector(RNG.RNG_kind.ordinal(), RNG.N01_kind.ordinal()));
  }

  /*
   * Primitives.
   */
  @Primitive("set.seed")
  public static void set_seed(int seed, int kind, int normalkind) {
    RNG.randomseed = seed;
    RNG.RNGkind(kind, normalkind);
    switch (RNG_kind) {
      case WICHMANN_HILL:
        throw new EvalException(RNG_kind + " not implemented yet");

      case MARSAGLIA_MULTICARRY:
        throw new EvalException(RNG_kind + " not implemented yet");

      case SUPER_DUPER:
        throw new EvalException(RNG_kind + " not implemented yet");

      case MERSENNE_TWISTER:
        if (mersenneTwisterAlg == null) {
          mersenneTwisterAlg = new MersenneTwister(seed);
        } else {
          mersenneTwisterAlg.setSeed(seed);
        }
        return;

      case KNUTH_TAOCP:
      case KNUTH_TAOCP2:
        throw new EvalException(RNG_kind + " not implemented yet");
      case USER_UNIF:
        throw new EvalException(RNG_kind + " not implemented yet");
      default:
        throw new EvalException(RNG_kind + " not implemented yet");
    }
  }

  @Primitive
  public static DoubleVector runif(int n, double a, double b) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(a + unif_rand() * (b - a));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rnorm(int n, double mean, double sd) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Normal.rnorm(mean, sd));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rgamma(int n, double shape, double scale) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Gamma.rgamma(shape, scale));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rchisq(int n, double df) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(ChiSquare.rchisq(df));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rnchisq(int n, double df, double ncp) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(ChiSquare.rnchisq(df, ncp));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rexp(int n, double invrate) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Exponantial.rexp(invrate));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rpois(int n, double mu) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Poisson.rpois(mu));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rsignrank(int nn, double n) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < nn; i++) {
      vb.add(SignRank.rsignrank(n));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rwilcox(int nn, double m, double n) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < nn; i++) {
      vb.add(Wilcox.rwilcox(m, n));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rgeom(int n, double p) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Geometric.rgeom(p));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rt(int n, double df) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(StudentsT.rt(df));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rcauchy(int n, double location, double scale) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Cauchy.rcauchy(location, scale));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rlnorm(int n, double meanlog, double sdlog) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(LNorm.rlnorm(meanlog, sdlog));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rlogis(int n, double location, double scale) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(RLogis.rlogis(location, scale));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rweibull(int n, double shape, double scale) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Weibull.rweibull(shape, scale));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rnbinom(int n, double size, double prob) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(NegativeBinom.rnbinom(size, prob));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rnbinom_mu(int n, double size, double mu) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(NegativeBinom.rnbinom_mu(size, mu));
    }
    return (vb.build());
  }
  
  @Primitive
  public static DoubleVector rbinom(int n, double size, double prob) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Binom.rbinom(size, prob));
    }
    return (vb.build());
  }
  
  
  @Primitive
  public static DoubleVector rf(int n, double df1, double df2) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(F.rf(df1, df2));
    }
    return (vb.build());
  }

  @Primitive
  public static DoubleVector rbeta(int n, double shape1, double shape2) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Beta.rbeta(shape1, shape2));
    }
    return (vb.build());
  }
  
  @Primitive
  public static DoubleVector rhyper(int nn, double m, double n, double k){
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(HyperGeometric.Random_hyper_geometric.rhyper(m, n, k));
    }
    return (vb.build());
  }

  /*
   * One of the Most important method in RNG
   * Before creating a random number from the distribution D,
   * we generate a uniform distributed random variable. 
   * 
   * Generated random numbers depend on the algorithm used.
   * As in original interpreter, the default algorithm is MERSENNE_TWISTER.
   * 
   * MERSENNE_TWISTER algorithm is imported from the apache commons math api.
   * But there is a small problem with this. The original interpreter and the renjin
   * produces different pseudo random numbers even the seed is same.
   * 
   * I am leaving this as is, I think it is not a real problem for now, somebody can 
   * correct the mechanism underlying the uniform random number generation for consistency 
   * with the original interpreter. 
   * 
   * Because I have not got the desired outputs, I can not test my generated random numbers. But one can 
   * see that, for example a sample of 1000 random numbers from a Chisquare(15) distribution has an
   * average of nearly 15. Similarly, a sample drawn from a Normal (0,1) distribution has a mean and variance
   * nearly zero and one, respectively.
   * 
   * mhsatman
   */
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
    switch (RNG_kind) {

      case WICHMANN_HILL:
        throw new EvalException(RNG_kind + " not implemented yet");

      case MARSAGLIA_MULTICARRY:
        throw new EvalException(RNG_kind + " not implemented yet");

      case SUPER_DUPER:
        throw new EvalException(RNG_kind + " not implemented yet");

      case MERSENNE_TWISTER:
        if (mersenneTwisterAlg == null) {
          mersenneTwisterAlg = new MersenneTwister(sseed);
        } else {
          mersenneTwisterAlg.setSeed(sseed);
        }
        return;

      case KNUTH_TAOCP:
      case KNUTH_TAOCP2:
        throw new EvalException(RNG_kind + " not implemented yet");
      case USER_UNIF:
        throw new EvalException(RNG_kind + " not implemented yet");
      default:
        throw new EvalException(RNG_kind + " not implemented yet");
    }
  }
}
