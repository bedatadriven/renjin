package r.base.random;

import org.apache.commons.math.stat.StatUtils;
import r.EvalTestCase;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import org.junit.Test;
import r.lang.DoubleVector;

public class TestSampling {

  @Test
  public void SamplingWithReplacement() {
    double delta = 0.00001;
    DoubleVector dv = new DoubleVector(new double[]{1, 2, 3, 4, 5});
    DoubleVector probs = new DoubleVector(new double[]{0.00, 0.00, 0.00, 1, 0.00});
    DoubleVector result = Sampling.sample(dv, 2, true, probs);
    assertThat(result.get(0), closeTo(4, delta));
  }

  @Test
  public void SamplingWithReplacementUniform() {
    double delta = 0.1;
    DoubleVector dv = new DoubleVector(new double[]{1, 2, 3, 4, 5});
    DoubleVector result = Sampling.sample(dv, 10000, true, null);
    assertThat(StatUtils.mean(result.toDoubleArray()), closeTo(3, delta));
  }

  @Test
  public void SamplingWithoutReplacement() {
    double delta = 0.00001;
    DoubleVector dv = new DoubleVector(new double[]{1, 2, 3, 4, 5, 10, 9, 8, 7, 6});
    DoubleVector result = Sampling.sample(dv, 5, false, null);
    System.out.println(result);
    for (int i = 0; i < result.length(); i++) {
      for (int j = i + 1; j < result.length(); j++) {
        if (i != j) {
          assert (result.get(i) != result.get(j));
        }
      }
    }
  }
}
