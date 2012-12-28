package org.renjin.primitives.random;

import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;


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

  /*
   * This algorithm is my own choice and somebody must speed it up at next level.
   * I plan to select the first index from {1,2,3,4,...,N} and
   * select the second one from {1,2,3,4,...,N-1} - {1st} and
   * select the third one from {1,2,3,4,...,N-2} - {1st, 2nd} and so on.
   * this operation requires more array copying but operations reduce geometrically by iterations.
   * Because of the first stage aim, I am leaving it as a running but in-efficient algorithm.
   * Tests were passed :)
   */
  public static IntVector sampleWithoutReplacement(Session context, int size, double[] prob) {
    double[] cumProbs = new double[prob.length];
    int[] selectedIndices = new int[prob.length];
    int numItems = 0;
    IntArrayVector.Builder resultb = new IntArrayVector.Builder();
    cumProbs[0] = prob[0];
    for (int i = 1; i < cumProbs.length; i++) {
      cumProbs[i] = prob[i] + cumProbs[i - 1];
    }

    while (numItems < size) {
      double arand = context.rng.unif_rand();
      int index = RouletteWheel(cumProbs, arand);
      if (selectedIndices[index] == 0) {
        selectedIndices[index] = 1;
        resultb.add(index + 1);
        numItems++;
      }
    }
    return (resultb.build());
  }

  @Primitive("sample")
  public static IntVector sample(@Current Session context, int x, int size, boolean replace, SEXP prob) {
    double[] probs = new double[x];
    int mysize = size;

    if (prob != org.renjin.sexp.Null.INSTANCE) {
      if (prob.length() != x) {
        throw new EvalException("Length of x and probs are not equal");
      }
    }


    for (int i = 0; i < x; i++) {
      if (prob == org.renjin.sexp.Null.INSTANCE) {
        probs[i] = 1.0 / probs.length;
      } else {
        probs[i] = ((DoubleVector)prob).get(i);
      }
    }


    if (replace) {
      return (sampleWithReplacement(context, mysize, probs));
    } else {
      return (sampleWithoutReplacement(context, mysize, probs));
    }


  }
}
