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
      // for large vectors, sum() returns a DeferredSummary
      eval(" x <- sum(as.double(1:1e5)) ");

      // for evaluation with I/O
      eval(" print(x) ");

      // ensure that the result is cached and correct
      assertThat(eval("x"), equalTo(c(5000050000d)));
    }
}
