package r.base.random;

import r.jvmi.annotations.Primitive;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.Null;
import r.lang.SEXP;
import r.lang.exception.EvalException;

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

  public static IntVector sampleWithReplacement(int size, double[] prob) {
    double[] cumProbs = new double[prob.length];
    IntVector.Builder resultb = new IntVector.Builder();
    cumProbs[0] = prob[0];
    for (int i = 1; i < cumProbs.length; i++) {
      cumProbs[i] = prob[i] + cumProbs[i - 1];
    }
    for (int i = 0; i < size; i++) {
      double arand = RNG.unif_rand();
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
  public static IntVector sampleWithoutReplacement(int size, double[] prob) {
    double[] cumProbs = new double[prob.length];
    int[] selectedIndices = new int[prob.length];
    int numItems = 0;
    IntVector.Builder resultb = new IntVector.Builder();
    cumProbs[0] = prob[0];
    for (int i = 1; i < cumProbs.length; i++) {
      cumProbs[i] = prob[i] + cumProbs[i - 1];
    }

    while (numItems < size) {
      double arand = RNG.unif_rand();
      int index = RouletteWheel(cumProbs, arand);
      if (selectedIndices[index] == 0) {
        selectedIndices[index] = 1;
        resultb.add(index + 1);
        numItems++;
      }
    }
    return (resultb.build());
  }

  @Primitive
  public static IntVector sample(int x, int size, boolean replace, SEXP prob) {
    double[] probs = new double[x];
    int mysize = size;

    if (prob != r.lang.Null.INSTANCE) {
      if (prob.length() != x) {
        throw new EvalException("Length of x and probs are not equal");
      }
    }


    for (int i = 0; i < x; i++) {
      if (prob == r.lang.Null.INSTANCE) {
        probs[i] = 1.0 / probs.length;
      } else {
        probs[i] = ((DoubleVector)prob).get(i);
      }
    }


    if (replace) {
      return (sampleWithReplacement(mysize, probs));
    } else {
      return (sampleWithoutReplacement(mysize, probs));
    }


  }
}
