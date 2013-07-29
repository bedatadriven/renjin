package org.renjin.primitives.vector;


import org.junit.Test;
import org.renjin.primitives.sequence.DoubleSequence;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Vector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class CombinedDoubleVectorTest {

  @Test
  public void test() {
    Vector vectors[] = new Vector[] {
      new DoubleSequence(1, 1, 100),
      new DoubleSequence(98, 2, 100),
      new DoubleArrayVector(5, 4, 3, 2)
    };

    DoubleVector combined = CombinedDoubleVector.combine(vectors, AttributeMap.EMPTY);

    assertThat(combined.length(), equalTo(204));
    assertThat(combined.getElementAsDouble(100), equalTo(vectors[1].getElementAsDouble(0)));
    assertThat(combined.getElementAsDouble(200), equalTo(5d));
    assertThat(combined.getElementAsDouble(203), equalTo(2d));

    // just verify that we can get all the elements without error
    for(int i=0;i!=combined.length();++i) {
      assertThat(combined.getElementAsDouble(i), not(equalTo(0d)));
    }

  }

}
