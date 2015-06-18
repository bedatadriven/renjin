package org.renjin.primitives.subset;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.EvalException;
import org.renjin.sexp.SEXP;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DataFrameSubsetTest extends EvalTestCase {

    @Test
    public void logical() {
      // for large vectors, sum() returns a DeferredSummary
      eval("df <- data.frame(x=1:10, y=21:30)");

      // for evaluation with I/O
      eval("df <- df[df$x>5,]");

      // ensure that the result is cached and correct
      assertThat(eval("df$y"), equalTo(eval("26:30")));
    }
}
