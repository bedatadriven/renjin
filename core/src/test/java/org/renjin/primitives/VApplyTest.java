/*
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
package org.renjin.primitives;

import org.junit.Before;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.EvalException;

import static org.junit.Assert.assertThat;


public class VApplyTest extends EvalTestCase {

  @Before
  public void defineVapply() {
    eval("vapply <- function (X, FUN, FUN.VALUE, ..., USE.NAMES = TRUE)  " +
        " .Internal(vapply(X, FUN, FUN.VALUE, USE.NAMES))");
  }
  
  @Test
  public void vapplySimple() {
    assertThat(eval("vapply(c(4,16,64), sqrt, 1)"), elementsIdenticalTo(c(2,4,8)));
  }
  
  @Test
  public void vapplyWithElipses() {
    assertThat(eval("vapply(1:4, `-`, 1, 1)"), elementsIdenticalTo(c(0,1,2,3)));
  }
  
  @Test
  public void names() {
    assertThat(eval("names(vapply(c(a=4,b=16,c=64), sqrt, 1))"), elementsIdenticalTo(c("a","b","c")));
  }
  
  @Test(expected=EvalException.class)
  public void vapplyTypeProblem() {
    eval("vapply(c(4,16,64), sqrt, TRUE)");
  }
}
