/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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
