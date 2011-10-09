package r.base.random;

import r.jvmi.annotations.Primitive;
import r.jvmi.annotations.Recycle;
import r.lang.DoubleVector;
import r.lang.exception.EvalException;

public class Sampling {

  public static int RouletteWheel(double[] cumulativeDist, double rand) {
    int result = 0;
    for (int i = 0; i < cumulativeDist.length - 1; i++) {
      if (rand > cumulativeDist[i] && rand < cumulativeDist[i + 1]) {
        return (i + 1);
      }
    }
    return (result);
  }

  public static DoubleVector sampleWithReplacement(DoubleVector x, int size, double[] prob) {
    double[] cumProbs = new double[prob.length];
    DoubleVector.Builder resultb = new DoubleVector.Builder();
    cumProbs[0] = prob[0];
    for (int i = 1; i < cumProbs.length; i++) {
      cumProbs[i] = prob[i] + cumProbs[i - 1];
    }
    for (int i = 0; i < size; i++) {
      double arand = RNG.unif_rand();
      int index = RouletteWheel(cumProbs, arand);
      resultb.add(x.get(index));
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
  public static DoubleVector sampleWithoutReplacement(DoubleVector x, int size, double[] prob) {
    double[] cumProbs = new double[prob.length];
    int[] selectedIndices = new int[prob.length];
    int numItems = 0;
    DoubleVector.Builder resultb = new DoubleVector.Builder();
    cumProbs[0] = prob[0];
    for (int i = 1; i < cumProbs.length; i++) {
      cumProbs[i] = prob[i] + cumProbs[i - 1];
    }
    while (numItems < size) {
      double arand = RNG.unif_rand();
      int index = RouletteWheel(cumProbs, arand);
      System.out.print("selected index: " + index);
      System.out.println(" and is it ok? " + selectedIndices[index]);
      if (selectedIndices[index] == 0) {
        selectedIndices[index] = 1;
        resultb.add(x.get(index));
        numItems++;
      }
    }
    return (resultb.build());
  }

  /*
   * how to make this primitive run in the renjin interpreter?
   * It says, expected parameters are double, int(1), logical(1), double. 
   * calling sample (1:10,2) does not run as expected.
   */
  @Primitive("sample")
  public static DoubleVector sample(DoubleVector x, int size, boolean replace, DoubleVector prob) {
    double[] probs = new double[x.length()];
    if (prob != null) {
      if (prob.length() != x.length()) {
        throw new EvalException("Number of elements of x and prob must be equal.");
      }
    }
    for (int i = 0; i < x.length(); i++) {
      if (prob == null) {
        probs[i] = 1.0 / probs.length;
      } else {
        probs[i] = prob.get(i);
      }
    }


    if (replace) {
      return (sampleWithReplacement(x, size, probs));
    } else {
      if (size > x.length()) {
        throw new EvalException("size can not be larger than the size of x when replace = FALSE");
      }
      return (sampleWithoutReplacement(x, size, probs));
    }
  }
}
