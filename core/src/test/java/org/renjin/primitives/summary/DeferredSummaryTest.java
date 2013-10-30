package org.renjin.primitives.summary;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.EvalException;
import org.renjin.sexp.SEXP;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DeferredSummaryTest extends EvalTestCase {

    @Test
    public void summation() {
      assertThat(eval("sum(c(1.5, 2.5))"), equalTo(c(4)));
    }
}
