package org.renjin.stats.internals.distributions;

import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.util.HeapsortTandem;


public class Sampling {

  /*
   * Nice function name, hah? :-)
   * A big reference goes to Holland and Goldberg
   */
  public static int RouletteWheel(double[] cumulativeDist, double rand) {
    int result = 0;
    for (int i = 0; i < cumulativeDist.length - 1; i++) {
      if (rand > cumulativeDist[i] && rand < cumulativeDist[i + 1]) {
        return (i + 1);
      }
    }
    return (result);
  }

  public static IntVector sampleWithReplacement(Session context, int size, double[] prob) {
    double[] cumProbs = new double[prob.length];
    IntArrayVector.Builder resultb = IntArrayVector.Builder.withInitialCapacity(size);
    cumProbs[0] = prob[0];
    for (int i = 1; i < cumProbs.length; i++) {
      cumProbs[i] = prob[i] + cumProbs[i - 1];
    }
    for (int i = 0; i < size; i++) {
      double arand = context.rng.unif_rand();
      int index = RouletteWheel(cumProbs, arand);
      resultb.add(index + 1);
    }
    return (resultb.build());
  }


  public static IntVector sampleWithoutReplacement(Session context, int sampleSpaceSize, int sampleSize) {
    int i, j;
    int[] x = new int[sampleSpaceSize];
    IntArrayVector.Builder y = new IntArrayVector.Builder();

    // Initialize the sample space 0 ... sampleSpaceSize
    for (i = 0; i < sampleSpaceSize; i++)
      x[i] = i;

    for (i = 0; i < sampleSize; i++) {
      j = (int)Math.floor(sampleSpaceSize * context.rng.unif_rand());
      y.add(x[j] + 1);
      x[j] = x[--sampleSpaceSize];
    }
    return (y.build());
  }

  /**
   * Generates a sample without replacement and with the given probabilities applied
   *
   * @param context    The Renjin Session context.
   * @param n          The size of the sample space
   * @param sampleSize The size of the sample
   * @return
   */
  public static IntVector probSampleWithoutReplacement(Session context, int n, int sampleSize, double[] probs) {
    int i, j;
    int[] x = new int[n];
    IntArrayVector.Builder y = new IntArrayVector.Builder();
    double rT, mass, totalmass;
    int k, n1;

    // Record element identities
    for (i = 0; i < n; i++)
      x[i] = i + 1;

    // Sort descending
    HeapsortTandem.heapsortDescending(probs, x, n);

    // Compute the sample
    totalmass = 1;
    for (i = 0, n1 = n - 1; i < sampleSize; i++, n1--) {
      rT = totalmass * context.rng.unif_rand();
      mass = 0;
      for (j = 0; j < n1; j++) {
        mass += probs[j];
        if (rT <= mass)
          break;
      }

      y.add(x[j]);
      totalmass -= probs[j];
      for (k = j; k < n1; k++) {
        probs[k] = probs[k + 1];
        x[k] = x[k + 1];
      }
    }
    return (y.build());
  }

  @Internal
  public static IntVector sample(@Current Session context, int x, int sampleSize, boolean replace, SEXP prob) {

    double[] probs;
    boolean probabilitiesGiven = (prob != org.renjin.sexp.Null.INSTANCE);
    if (probabilitiesGiven) {
       probs = ((DoubleVector) prob).toDoubleArray();
    }
    else probs = new double[x];

    if(probabilitiesGiven && prob.length() != x) {
      throw new EvalException("The number of probabilities should be the same as the sample "
          + " size.");
    }

    for (int i = 0; i < x; i++) {
      if (prob == org.renjin.sexp.Null.INSTANCE) {
        probs[i] = 1.0 / probs.length;
      } else {
        probs[i] = ((DoubleVector) prob).get(i);
      }
    }

    // GNU R allows "prob" to be a generalized list of weights, but they should be rescaled to proper probabilities
    weightsToProbabilities(probs, x, sampleSize, replace);

    if (replace) {
      return (sampleWithReplacement(context, sampleSize, probs));
    } else {
      IntVector response = (probabilitiesGiven) ?
          probSampleWithoutReplacement(context, x, sampleSize, probs) :
          sampleWithoutReplacement(context, x, sampleSize);
      return response;
    }
  }

  /**
   * Rescales a list of weights to probabilities
   */
  private static void weightsToProbabilities(double[] weights, int n, int sampleSize, boolean replace) {
    double sum;
    int i, npos;
    npos = 0;
    sum = 0.;
    for (i = 0; i < n; i++) {
      if (weights[i] < 0)
        throw new EvalException("non-positive probability");
      if (weights[i] > 0) {
        npos++;
        sum += weights[i];
      }
    }
    if (npos == 0 || (!replace && sampleSize > npos))
      throw new EvalException("too few positive probabilities");
    for (i = 0; i < n; i++)
      weights[i] /= sum;
  }
}
