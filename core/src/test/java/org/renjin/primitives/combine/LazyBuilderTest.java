package org.renjin.primitives.combine;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.DoubleVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class LazyBuilderTest extends EvalTestCase {

  @Test
  public void testBuild() {
    CombinedBuilder lazyBuilder = new LazyBuilder(DoubleVector.VECTOR_TYPE, 10).useNames(true);
    lazyBuilder.addElements(null, DoubleVector.valueOf(1));
    lazyBuilder.addElements(null, DoubleVector.valueOf(2));
    assertThat(lazyBuilder.build(), equalTo(c(1, 2)));
  }
}
