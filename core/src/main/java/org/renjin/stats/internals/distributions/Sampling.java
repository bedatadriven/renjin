/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.stats.internals.distributions;

import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.util.HeapsortTandem;

import java.util.HashMap;
import java.util.Map;


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


  /**
   * Sample without replacement using the  Durstenfeld-Fisher-Yates algorithm.
   */
  public static IntVector uniformSampleWithoutReplacement(Session context, int sampleSpaceSize, int sampleSize) {
    int i, j;
    int[] x = new int[sampleSpaceSize];
    IntArrayVector.Builder y = new IntArrayVector.Builder();

    // Initialize the sample space 0 ... sampleSpaceSize
    for (i = 0; i < sampleSpaceSize; i++) {
      x[i] = i;
    }

    for (i = 0; i < sampleSize; i++) {
      j = (int)Math.floor(sampleSpaceSize * context.rng.unif_rand());
      y.add(x[j] + 1);
      x[j] = x[--sampleSpaceSize];
    }
    return y.build();
  }

  /**
   * Modified version of the Durstenfeld-Fisher-Yatess algorithm for large sample spaces.
   */
  public static IntVector uniformSampleWithoutReplacementMap(Session context, int sampleSpaceSize, int sampleSize) {
    int i, j;
    Map<Integer, Integer> map = new HashMap<>();
    IntArrayVector.Builder y = new IntArrayVector.Builder();

    for (i = 0; i < sampleSize; i++) {
      j = (int)Math.floor(sampleSpaceSize * context.rng.unif_rand());
      int sampledIndex = map.getOrDefault(j, j);
      y.add(sampledIndex + 1);
      --sampleSpaceSize;
      map.put(j, map.getOrDefault(sampleSpaceSize, sampleSpaceSize));
    }
    return y.build();
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
  public static IntVector sample(@Current Session context, 
                                 int populationSize, 
                                 int sampleSize, 
                                 boolean withReplacement, 
                                 AtomicVector probabilityWeights) {

    boolean probabilitiesGiven = (probabilityWeights != org.renjin.sexp.Null.INSTANCE);
    
    if(!probabilitiesGiven) {
      if (withReplacement || sampleSize == 1) {
        return uniformSampleWithReplacement(context, populationSize, sampleSize);
      } else if (populationSize < 100) {
        return uniformSampleWithoutReplacement(context, populationSize, sampleSize);
      } else {
        return uniformSampleWithoutReplacementMap(context, populationSize, sampleSize);
      }
    }

    // GNU R allows "prob" to be a generalized list of weights, but they should be rescaled to proper probabilities
    double probs[] = weightsToProbabilities(probabilityWeights, populationSize, sampleSize, withReplacement);

    if (withReplacement) {
      return (sampleWithReplacement(context, sampleSize, probs));
    } else {
      return probSampleWithoutReplacement(context, populationSize, sampleSize, probs);
    }
  }

  /**
   * Samples {@code sampleSize} items from a population of the size {@code populationSize}.
   * 
   * @param context
   * @param populationSize the number of items in the population from which we are sampling
   * @param sampleSize the number of items to select from the population.
   * @return a vector containing the one-based indices of the sampled items.
   */
  private static IntVector uniformSampleWithReplacement(Session context,
                                                        int populationSize,
                                                        int sampleSize) {
    double dn = populationSize;
    int[] sample = new int[sampleSize];

    for (int i = 0; i < sample.length; ++i) {
      sample[i] = (int)Math.floor(dn * context.rng.unif_rand() + 1);
    }
    return new IntArrayVector(sample);
  }

  /**
   * Rescales a list of weights to probabilities
   */
  private static double[] weightsToProbabilities(AtomicVector weightVector, 
                                             int populationSize, 
                                             int sampleSize, 
                                             boolean replace) {
    
    if(weightVector.length() != populationSize) {
      throw new EvalException("incorrect number of probabilities");
    }

    double weights[] = weightVector.toDoubleArray();
    
    double sum;
    int i, npos;
    npos = 0;
    sum = 0.;
    for (i = 0; i < populationSize; i++) {
      if (weights[i] < 0)
        throw new EvalException("non-positive probability");
      if (weights[i] > 0) {
        npos++;
        sum += weights[i];
      }
    }
    if (npos == 0 || (!replace && sampleSize > npos)) {
      throw new EvalException("too few positive probabilities");
    }
    for (i = 0; i < populationSize; i++) {
      weights[i] /= sum;
    }
    
    return weights;
  }
}
