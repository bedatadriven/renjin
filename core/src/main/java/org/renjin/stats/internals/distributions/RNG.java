package org.renjin.stats.internals.distributions;


import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.primitives.System;
import org.renjin.sexp.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;


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
  public static DoubleVector runif(@Current Context context, Vector nVector, Vector min, Vector max) {
    int n = defineSize(nVector);
    RNG rng = context.getSession().rng;
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      if ( max.length() == 0 || (max.length() != 1 && j == max.length()-1)) j = 0;
      if ( min.length() == 0 || (min.length() != 1 && k == min.length()-1)) k = 0;
      vb.add(min.getElementAsDouble(k) + rng.unif_rand() * (max.getElementAsDouble(j) - min.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnorm(@Current Context context, Vector nVector, Vector mean, Vector sd) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      if (mean.length() != 1 && j == mean.length()-1) k = 0;
      if (sd.length() != 1 && k == sd.length()-1) j = 0;
      vb.add(Normal.rnorm(context.getSession(), mean.getElementAsDouble(j), sd.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rgamma(@Current Context context, Vector nVector, Vector shape, Vector scale) {
    int n  = defineSize(nVector);
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      if (shape.length() != 1 && j == shape.length()-1) j = 0;
      if (scale.length() != 1 && k == scale.length()-1) k = 0;
      vb.add(Gamma.rgamma(context.getSession(), shape.getElementAsDouble(j), scale.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rchisq(@Current Context context, Vector nVector, Vector df) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0;
    for (int i = 0; i < n; i++) {
      if (df.length() != 1 && j == df.length()-1) j = 0;
      vb.add(ChiSquare.rchisq(context.getSession(), df.getElementAsDouble(j)));
      j++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnchisq(@Current Context context, Vector nVector, Vector df, double ncp) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0;
    for (int i = 0; i < n; i++) {
      if (df.length() != 1 && j == df.length()-1) j = 0;
      vb.add(ChiSquare.rnchisq(context.getSession(), df.getElementAsDouble(j), ncp));
      j++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rexp(@Current Context context, Vector nVector, Vector invrate) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0;
    for (int i = 0; i < n; i++) {
      if (invrate.length() != 1 && j == invrate.length()-1) j = 0;
      vb.add(Exponantial.rexp(context.getSession(), invrate.getElementAsDouble(j)));
      j++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rpois(@Current Context context, Vector nVector, Vector mu) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0;
    for (int i = 0; i < n; i++) {
      if (mu.length() != 1 && j == mu.length()-1) j = 0;
      vb.add(Poisson.rpois(context.getSession(), mu.getElementAsDouble(j)));
      j++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rsignrank(@Current Context context, Vector nnVector, Vector n) {
    int nn = defineSize(nnVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0;
    for (int i = 0; i < nn; i++) {
      if (n.length() != 1 && j == n.length()-1) j = 0;
      vb.add(SignRank.rsignrank(context.getSession(), n.getElementAsDouble(j)));
      j++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rwilcox(@Current Context context, Vector nnVector, Vector mVector, Vector n) {
    int nn = defineSize(nnVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0, k = 0;
    for (int i = 0; i < nn; i++) {
      if (mVector.length() != 1 && j == mVector.length()-1) j = 0;
      if (n.length() != 1 && k == n.length()-1) k = 0;
      vb.add(Wilcox.rwilcox(context.getSession(), mVector.getElementAsDouble(j), n.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rgeom(@Current Context context, Vector nVector, Vector p) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0;
    for (int i = 0; i < n; i++) {
      if (p.length() != 1 && j == p.length()-1) j = 0;
      vb.add(Geometric.rgeom(context.getSession(), p.getElementAsDouble(j)));
      j++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rt(@Current Context context, Vector nVector, Vector df) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0;
    for (int i = 0; i < n; i++) {
      if (df.length() != 1 && j == df.length()-1) j = 0;
      vb.add(StudentsT.rt(context.getSession(), df.getElementAsDouble(j)));
      j++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rcauchy(@Current Context context, Vector nVector, Vector location, Vector scale) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      if (location.length() != 1 && j == location.length()-1) j = 0;
      if (scale.length() != 1 && k == scale.length()-1) k = 0;
      vb.add(Cauchy.rcauchy(context.getSession(), location.getElementAsDouble(j), scale.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rlnorm(@Current Context context, Vector nVector, Vector meanlog, Vector sdlog) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      if (meanlog.length() != 1 && j == meanlog.length()-1) j = 0;
      if (sdlog.length() != 1 && k == sdlog.length()-1) k = 0;
      vb.add(LNorm.rlnorm(context.getSession(), meanlog.getElementAsDouble(j), sdlog.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rlogis(@Current Context context, Vector nVector, Vector location, Vector scale) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      if (location.length() != 1 && j == location.length()-1) j = 0;
      if (scale.length() != 1 && k == scale.length()-1) k = 0;
      vb.add(RLogis.rlogis(context.getSession(), location.getElementAsDouble(j), scale.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rweibull(@Current Context context, Vector nVector, Vector shape, Vector scale) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      if (shape.length() != 1 && j == shape.length()-1) j = 0;
      if (scale.length() != 1 && k == scale.length()-1) k = 0;
      vb.add(Weibull.rweibull(context.getSession(), shape.getElementAsDouble(j), scale.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnbinom(@Current Context context, Vector nVector, Vector sizeVector, Vector prob) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      if (sizeVector.length() != 1 && j == sizeVector.length()-1) j = 0;
      if (prob.length() != 1 && k == prob.length()-1) k = 0;
      vb.add(NegativeBinom.rnbinom(context.getSession(), sizeVector.getElementAsDouble(j), prob.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnbinom_mu(@Current Context context, Vector nVector, Vector size, Vector mu) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      if (size.length() != 1 && j == size.length()-1) j = 0;
      if (mu.length() != 1 && k == mu.length()-1) k = 0;
      vb.add(NegativeBinom.rnbinom_mu(context.getSession(), size.getElementAsDouble(j), mu.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rbinom(@Current Context context, Vector nVector, Vector size, Vector prob) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      if (size.length() != 1 && j == size.length()-1) j = 0;
      if (prob.length() != 1 && k == prob.length()-1) k = 0;
      vb.add(Binom.rbinom(context.getSession(), size.getElementAsDouble(j), prob.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }


  @Internal
  public static DoubleVector rf(@Current Context context, Vector nVector, Vector df1, Vector df2) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      if (df1.length() != 1 && j == df1.length()-1) j = 0;
      if (df2.length() != 1 && k == df2.length()-1) k = 0;
      vb.add(F.rf(context.getSession(), df1.getElementAsDouble(j), df2.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rbeta(@Current Context context, Vector nVector, Vector shape1, Vector shape2) {
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      if (shape1.length() != 1 && j == shape1.length()-1) j = 0;
      if (shape2.length() != 1 && k == shape2.length()-1) k = 0;
      vb.add(Beta.rbeta(context.getSession(), shape1.getElementAsDouble(j), shape2.getElementAsDouble(k)));
      j++;
      k++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rhyper(@Current Context context, Vector nnVector, Vector m, Vector n, Vector k) {
    int nn = defineSize(nnVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int j = 0, p = 0, q = 0;
    for (int i = 0; i < nn; i++) {
      if (m.length() != 1 && j == m.length()-1) j = 0;
      if (n.length() != 1 && p == n.length()-1) p = 0;
      if (k.length() != 1 && q == k.length()-1) q = 0;
      vb.add(HyperGeometric.Random_hyper_geometric.rhyper(
              context.getSession(), m.getElementAsDouble(j), n.getElementAsDouble(p), k.getElementAsDouble(q)));
      j++;
      p++;
      q++;
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rmultinom(@Current Context context, Vector nVector, Vector size, DoubleVector prob){
    int n = defineSize(nVector);
    DoubleArrayVector.Builder vb = new DoubleArrayVector.Builder();
    int k = 0;
    int[] RN = new int[prob.length()];
    for (int i=0;i<n;i++){
      if (size.length() != 1 && k == size.length()-1) k = 0;
      Multinomial.rmultinom(context.getSession(), size.getElementAsInt(k), prob.toDoubleArray(), prob.length(), RN);
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

  public static int defineSize(Vector input) {
    return (input.length() == 1) ? input.getElementAsInt(0) : input.length();
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
