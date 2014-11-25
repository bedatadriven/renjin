package org.renjin.stats.internals.distributions;


import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.*;


public class RNG {

  public MersenneTwister mersenneTwisterAlg = null;
  public RNGtype RNG_kind = RNGtype.MERSENNE_TWISTER; //default
  public N01type N01_kind = N01type.INVERSION; //default
  int randomseed = 0;
  public Session context;


  public RNG(Session globals){
    this.context = globals;
  }

  @Internal
  public static IntVector RNGkind(@Current Context context, SEXP kindExp, SEXP normalkindExp) {
    RNG rng = context.getSession().rng;  
    
    if(kindExp != Null.INSTANCE) {
      int kind = ((AtomicVector)kindExp).getElementAsInt(0);
      try {
        rng.RNG_kind = RNGtype.values()[kind];
      } catch (Exception e) {
        throw new EvalException("RNGkind: unimplemented RNG kind " + kind);
      }
    }
    if(normalkindExp != Null.INSTANCE) {
      int normalkind = ((AtomicVector)normalkindExp).getElementAsInt(0);
      try {
        rng.N01_kind = N01type.values()[normalkind];
      } catch (Exception e) {
        throw new EvalException("invalid Normal type in RNGkind");
      }
    } 

    return (new IntArrayVector(rng.RNG_kind.ordinal(), rng.N01_kind.ordinal()));
  }

  /*
   * Primitives.
   */
  @Internal("set.seed")
  public static void set_seed(@Current Context context, int seed, SEXP kind, SEXP normalkind) {
    RNG rng = context.getSession().rng;
    rng.randomseed = seed;
    RNGkind(context, kind, normalkind);
    switch (rng.RNG_kind) {
    case WICHMANN_HILL:
      throw new EvalException(rng.RNG_kind + " not implemented yet");

    case MARSAGLIA_MULTICARRY:
      throw new EvalException(rng.RNG_kind + " not implemented yet");

    case SUPER_DUPER:
      throw new EvalException(rng.RNG_kind + " not implemented yet");

    case MERSENNE_TWISTER:
      if (rng.mersenneTwisterAlg == null) {
        rng.mersenneTwisterAlg = new MersenneTwister(seed);
      } else {
        rng.mersenneTwisterAlg.setSeed(seed);
      }
      return;

    case KNUTH_TAOCP:
    case KNUTH_TAOCP2:
      throw new EvalException(rng.RNG_kind + " not implemented yet");
    case USER_UNIF:
      throw new EvalException(rng.RNG_kind + " not implemented yet");
    default:
      throw new EvalException(rng.RNG_kind + " not implemented yet");
    }
  }

  @Internal
  public static DoubleVector runif(@Current Context context, int n, double a, double b) {
    RNG rng = context.getSession().rng;
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    for (int i = 0; i < n; i++) {
      vb.add(a + rng.unif_rand() * (b - a));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnorm(@Current Context context, int n, double mean, double sd) {
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    for (int i = 0; i < n; i++) {
      vb.add(Normal.rnorm(context.getSession(), mean, sd));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rgamma(@Current Context context, int n, double shape, double scale) {
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    for (int i = 0; i < n; i++) {
      vb.add(Gamma.rgamma(context.getSession(), shape, scale));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rchisq(@Current Context context, int n, double df) {
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    for (int i = 0; i < n; i++) {
      vb.add(ChiSquare.rchisq(context.getSession(), df));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnchisq(@Current Context context, int n, double df, double ncp) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(ChiSquare.rnchisq(context.getSession(), df, ncp));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rexp(@Current Context context, int n, double invrate) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Exponantial.rexp(context.getSession(), invrate));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rpois(@Current Context context, int n, double mu) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Poisson.rpois(context.getSession(), mu));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rsignrank(@Current Context context, int nn, double n) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < nn; i++) {
      vb.add(SignRank.rsignrank(context.getSession(), n));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rwilcox(@Current Context context, int nn, double m, double n) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < nn; i++) {
      vb.add(Wilcox.rwilcox(context.getSession(), m, n));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rgeom(@Current Context context, int n, double p) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Geometric.rgeom(context.getSession(), p));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rt(@Current Context context, int n, double df) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(StudentsT.rt(context.getSession(), df));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rcauchy(@Current Context context, int n, double location, double scale) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Cauchy.rcauchy(context.getSession(), location, scale));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rlnorm(@Current Context context, int n, double meanlog, double sdlog) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(LNorm.rlnorm(context.getSession(), meanlog, sdlog));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rlogis(@Current Context context, int n, double location, double scale) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(RLogis.rlogis(context.getSession(), location, scale));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rweibull(@Current Context context, int n, double shape, double scale) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Weibull.rweibull(context.getSession(), shape, scale));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnbinom(@Current Context context, int n, double size, double prob) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(NegativeBinom.rnbinom(context.getSession(), size, prob));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnbinom_mu(@Current Context context, int n, double size, double mu) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(NegativeBinom.rnbinom_mu(context.getSession(), size, mu));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rbinom(@Current Context context, int n, double size, double prob) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Binom.rbinom(context.getSession(), size, prob));
    }
    return (vb.build());
  }


  @Internal
  public static DoubleVector rf(@Current Context context, int n, double df1, double df2) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(F.rf(context.getSession(), df1, df2));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rbeta(@Current Context context, int n, double shape1, double shape2) {
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Beta.rbeta(context.getSession(), shape1, shape2));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rhyper(@Current Context context, int nn, double m, double n, double k){
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    for (int i = 0; i < nn; i++) {
      vb.add(HyperGeometric.Random_hyper_geometric.rhyper(context.getSession(), m, n, k));
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rmultinom(@Current Context context, int n, int size, DoubleVector prob){
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int[] RN = new int[prob.length()];
    for (int i=0;i<n;i++){
      Multinomial.rmultinom(context.getSession(), size, prob.toDoubleArray(), prob.length(), RN);
      for (int j = 0; j < prob.length(); j++) {
        vb.add(RN[j]);
      }
    }
    vb.setAttribute(Symbols.DIM, new IntArrayVector(prob.length(), n));
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
   * 
   * mhsatman
   */
  public double unif_rand() {
    double value;

    switch (this.RNG_kind) {

    case WICHMANN_HILL:
      throw new EvalException(RNG_kind + " not implemented yet");

    case MARSAGLIA_MULTICARRY:
      throw new EvalException(RNG_kind + " not implemented yet");

    case SUPER_DUPER:
      throw new EvalException(RNG_kind + " not implemented yet");

    case MERSENNE_TWISTER:
      if (mersenneTwisterAlg == null) {
        if (this.randomseed == 0) {
          Randomize(RNG_kind);
        }
        mersenneTwisterAlg = new MersenneTwister((long) this.randomseed);
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
  public void Randomize(RNGtype kind) {
    int sseed;
    sseed = (int) (new java.util.Date()).getTime();
    this.randomseed = sseed;
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
