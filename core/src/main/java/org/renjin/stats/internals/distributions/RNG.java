package org.renjin.stats.internals.distributions;


import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.primitives.System;
import org.renjin.repackaged.guava.base.Strings;
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
  public static DoubleVector runif(@Current Context context, Vector nVector, AtomicVector min, AtomicVector max) {
    int n = defineSize(nVector);
    int minLength = min.length();
    int maxLength = min.length();
    RNG rng = context.getSession().rng;
    if (minLength == 0 || maxLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(min.getElementAsDouble(j) + rng.unif_rand() * (max.getElementAsDouble(k) - min.getElementAsDouble(j)));
      j++;
      k++;
      if (j == minLength) {
        j = 0;
      }
      if (k == maxLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnorm(@Current Context context, Vector nVector, AtomicVector mean, AtomicVector sd) {
    int n = defineSize(nVector);
    int meanLength  = mean.length();
    int sdLength = sd.length();
    if (meanLength == 0 || sdLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
      int j = 0, k = 0;
      for (int i = 0; i < n; i++) {
        vb.add(Normal.rnorm(context.getSession(), mean.getElementAsDouble(j), sd.getElementAsDouble(k)));
        j++;
        k++;
        if (j == meanLength) {
          j = 0;
        }
        if (k == sdLength) {
          k = 0;
        }
      }
      return (vb.build());
  }

  @Internal
  public static DoubleVector rgamma(@Current Context context, Vector nVector, AtomicVector shape, AtomicVector scale) {
    int n  = defineSize(nVector);
    int shapeLength = shape.length();
    int scaleLength = scale.length();
    if (shapeLength == 0 || scaleLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(Gamma.rgamma(context.getSession(), shape.getElementAsDouble(j), scale.getElementAsDouble(k)));
      j++;
      k++;
      if (j == shapeLength) {
        j = 0;
      }
      if (k == scaleLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rchisq(@Current Context context, Vector nVector, AtomicVector df) {
    int n = defineSize(nVector);
    int dfLength = df.length();
    if (dfLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0;
    for (int i = 0; i < n; i++) {
      vb.add(ChiSquare.rchisq(context.getSession(), df.getElementAsDouble(j)));
      j++;
      if (j == dfLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnchisq(@Current Context context, Vector nVector, AtomicVector df, double ncp) {
    int n = defineSize(nVector);
    int dfLength = df.length();
    if (dfLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0;
    for (int i = 0; i < n; i++) {
      vb.add(ChiSquare.rnchisq(context.getSession(), df.getElementAsDouble(j), ncp));
      j++;
      if (j == dfLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rexp(@Current Context context, Vector nVector, AtomicVector invrate) {
    int n = defineSize(nVector);
    int invrateLength = invrate.length();
    if (invrateLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0;
    for (int i = 0; i < n; i++) {
      vb.add(Exponantial.rexp(context.getSession(), invrate.getElementAsDouble(j)));
      j++;
      if (j == invrateLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rpois(@Current Context context, Vector nVector, AtomicVector mu) {
    int n = defineSize(nVector);
    int muLength = mu.length();
    if (muLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0;
    for (int i = 0; i < n; i++) {
      vb.add(Poisson.rpois(context.getSession(), mu.getElementAsDouble(j)));
      j++;
      if (j == muLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rsignrank(@Current Context context, Vector nnVector, AtomicVector n) {
    int nn = defineSize(nnVector);
    int nLength = n.length();
    if (nLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(nn).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(nn);
    int j = 0;
    for (int i = 0; i < nn; i++) {
      vb.add(SignRank.rsignrank(context.getSession(), n.getElementAsDouble(j)));
      j++;
      if (j == nLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rwilcox(@Current Context context, Vector nnVector, AtomicVector m, AtomicVector n) {
    int nn = defineSize(nnVector);
    int mLength = m.length();
    int nLength = n.length();
    if (mLength == 0 || nLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(nn).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(nn);
    int j = 0, k = 0;
    for (int i = 0; i < nn; i++) {
      vb.add(Wilcox.rwilcox(context.getSession(), m.getElementAsDouble(j), n.getElementAsDouble(k)));
      j++;
      k++;
      if (j == mLength) {
        j = 0;
      }
      if (k == nLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rgeom(@Current Context context, Vector nVector, AtomicVector p) {
    int n = defineSize(nVector);
    int pLength = p.length();
    if (pLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0;
    for (int i = 0; i < n; i++) {
      vb.add(Geometric.rgeom(context.getSession(), p.getElementAsDouble(j)));
      j++;
      if (j == pLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rt(@Current Context context, Vector nVector, AtomicVector df) {
    int n = defineSize(nVector);
    int dfLength = df.length();
    if (dfLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0;
    for (int i = 0; i < n; i++) {
      vb.add(StudentsT.rt(context.getSession(), df.getElementAsDouble(j)));
      j++;
      if (j == dfLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rcauchy(@Current Context context, Vector nVector, AtomicVector location, AtomicVector scale) {
    int n = defineSize(nVector);
    int locationLength = location.length();
    int scaleLength = scale.length();
    if (locationLength == 0 || scaleLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(Cauchy.rcauchy(context.getSession(), location.getElementAsDouble(j), scale.getElementAsDouble(k)));
      j++;
      k++;
      if (j == locationLength) {
        j = 0;
      }
      if (k == scaleLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rlnorm(@Current Context context, Vector nVector, AtomicVector meanlog, AtomicVector sdlog) {
    int n = defineSize(nVector);
    int meanlogLenght = meanlog.length();
    int sdlogLength = sdlog.length();
    if (meanlogLenght == 0 || sdlogLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(LNorm.rlnorm(context.getSession(), meanlog.getElementAsDouble(j), sdlog.getElementAsDouble(k)));
      j++;
      k++;
      if (j == meanlogLenght) {
        j = 0;
      }
      if (k == sdlogLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rlogis(@Current Context context, Vector nVector, AtomicVector location, AtomicVector scale) {
    int n = defineSize(nVector);
    int locationLength = location.length();
    int scaleLength = scale.length();
    if (locationLength == 0 || scaleLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(RLogis.rlogis(context.getSession(), location.getElementAsDouble(j), scale.getElementAsDouble(k)));
      j++;
      k++;
      if (j == locationLength) {
        j = 0;
      }
      if (k == scaleLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rweibull(@Current Context context, Vector nVector, AtomicVector shape, AtomicVector scale) {
    int n = defineSize(nVector);
    int shapeLength = shape.length();
    int scaleLength = scale.length();
    if (shapeLength == 0 || scaleLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(Weibull.rweibull(context.getSession(), shape.getElementAsDouble(j), scale.getElementAsDouble(k)));
      j++;
      k++;
      if (j == shapeLength) {
        j = 0;
      }
      if (k == scaleLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnbinom(@Current Context context, Vector nVector, AtomicVector size, AtomicVector prob) {
    int n = defineSize(nVector);
    int sizeLength = size.length();
    int probLength = prob.length();
    if (sizeLength == 0 || probLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(NegativeBinom.rnbinom(context.getSession(), size.getElementAsDouble(j), prob.getElementAsDouble(k)));
      j++;
      k++;
      if (j == sizeLength) {
        j = 0;
      }
      if (k == probLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnbinom_mu(@Current Context context, Vector nVector, AtomicVector size, AtomicVector mu) {
    int n = defineSize(nVector);
    int sizeLength = size.length();
    int muLenght = mu.length();
    if (sizeLength == 0 || muLenght == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(NegativeBinom.rnbinom_mu(context.getSession(), size.getElementAsDouble(j), mu.getElementAsDouble(k)));
      j++;
      k++;
      if (j == sizeLength) {
        j = 0;
      }
      if (k == muLenght) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rbinom(@Current Context context, Vector nVector, AtomicVector size, AtomicVector prob) {
    int n = defineSize(nVector);
    int sizeLength = size.length();
    int probLength = prob.length();
    if (sizeLength == 0 || probLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(Binom.rbinom(context.getSession(), size.getElementAsDouble(j), prob.getElementAsDouble(k)));
      j++;
      k++;
      if (j == sizeLength) {
        j = 0;
      }
      if (k == probLength) {
        k = 0;
      }
    }
    return (vb.build());
  }


  @Internal
  public static DoubleVector rf(@Current Context context, Vector nVector, AtomicVector df1, AtomicVector df2) {
    int n = defineSize(nVector);
    int df1Length = df1.length();
    int df2Length = df2.length();
    if (df1Length == 0 || df2.length() == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(F.rf(context.getSession(), df1.getElementAsDouble(j), df2.getElementAsDouble(k)));
      j++;
      k++;
      if (j == df1Length) {
        j = 0;
      }
      if (k == df2Length) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rbeta(@Current Context context, Vector nVector, AtomicVector shape1, AtomicVector shape2) {
    int n = defineSize(nVector);
    int shape1Length = shape1.length();
    int shape2Length = shape2.length();
    if (shape1Length == 0 || shape2Length == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(Beta.rbeta(context.getSession(), shape1.getElementAsDouble(j), shape2.getElementAsDouble(k)));
      j++;
      k++;
      if (j == shape1Length) {
        j = 0;
      }
      if (k == shape2Length) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rhyper(@Current Context context, Vector nnVector, AtomicVector m, AtomicVector n, AtomicVector k) {
    int nn = defineSize(nnVector);
    int mLength = m.length();
    int nLength = n.length();
    int kLength = k.length();
    if (mLength == 0 || nLength == 0 || kLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(nn).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(nn);
    int j = 0, p = 0, q = 0;
    for (int i = 0; i < nn; i++) {
      vb.add(HyperGeometric.Random_hyper_geometric.rhyper(
              context.getSession(), m.getElementAsDouble(j), n.getElementAsDouble(p), k.getElementAsDouble(q)));
      j++;
      p++;
      q++;
      if (j == mLength) {
        j = 0;
      }
      if (p == nLength) {
        p = 0;
      }
      if (q == kLength) {
        q = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rmultinom(@Current Context context, Vector nVector, AtomicVector size, AtomicVector prob){
    int n = defineSize(nVector);
    int sizeLength = size.length();
    int probLength = prob.length();
    if (sizeLength == 0 || probLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    int k = 0;
    int[] RN = new int[probLength];
    for (int i = 0; i < n; i++){
      Multinomial.rmultinom(context.getSession(), size.getElementAsInt(k), prob.toDoubleArray(), probLength, RN);
      k++;
      if (k == sizeLength) {
        k = 0;
      }
      for (int j = 0; j < probLength; j++) {
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
    int inputLength = (input.length() == 1) ? input.getElementAsInt(0) : input.length();
    if (input.length() == 0 || (input.length() == 1 && (input.isElementNA(0) || input.isElementNaN(0)))) {
      throw new EvalException("invalid arguments.");
    }
    return inputLength;
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
