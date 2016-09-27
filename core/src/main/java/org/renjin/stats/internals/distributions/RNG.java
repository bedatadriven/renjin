package org.renjin.stats.internals.distributions;


import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
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
  private MethodHandle methodHandle;


  public RNG(Session globals){
    this.context = globals;
    this.methodHandle = createMethodHandle(this);
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


  private static MethodHandle createMethodHandle(RNG rng) {
    try {
      MethodHandle instanceMethod = MethodHandles.publicLookup().findVirtual(RNG.class, "unif_rand",
              MethodType.methodType(double.class));
      MethodHandle staticMethod = MethodHandles.insertArguments(instanceMethod, 0, rng);

      return staticMethod;

    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalStateException("getMethodHandle() failed: " + e.getMessage(), e);
    }
  }

  public MethodHandle getMethodHandle() {
    return methodHandle;
  }

}
