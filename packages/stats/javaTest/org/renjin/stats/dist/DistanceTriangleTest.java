package org.renjin.stats.dist;

import org.junit.Test;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleArrayVector;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;


public class DistanceTriangleTest {

  @Test
  public void coordinates() {

    DoubleArrayVector vector = new DoubleArrayVector(0,100,300);
    Euclidean1dDistanceTriangle triangle = new Euclidean1dDistanceTriangle(vector, AttributeMap.EMPTY);

    assertThat(triangle.get(0), equalTo(100d));
    assertThat(triangle.get(1), equalTo(300d));
    assertThat(triangle.get(2), equalTo(200d));

    vector = new DoubleArrayVector(8, 4, 1, 7, 0, 3, 6, 1, 9, 2);
    triangle = new Euclidean1dDistanceTriangle(vector, AttributeMap.EMPTY);
    
    assertThat(triangle.toDoubleArray(), equalTo(new double[] {
        4, 7, 1, 8, 5, 2, 7, 1, 6, 3, 3, 4, 1, 2, 3, 5, 2, 6, 1, 2,  5, 0,
        8, 1, 7, 4, 1, 6, 2, 5, 3, 6, 1, 9, 2, 3, 2, 6, 1, 5, 3,  4, 8, 1, 7,
    }));
     
  }
}
