/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.stats.internals;

import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.junit.Assert.assertThat;

public class CompleteCasesTest extends EvalTestCase {

  @Test
  public void test() {
    assertThat(eval(".Internal(complete.cases(1:3, 1:3))"), elementsIdenticalTo(c(true, true, true)));
    assertThat(eval(".Internal(complete.cases(1:3, c(1,NA,2)))"), elementsIdenticalTo(c(true, false, true)));
    assertThat(eval(".Internal(complete.cases(list(1:3,1:3), 1:3, 1:3))"), 
        elementsIdenticalTo(c(true, true, true)));
  }
  
  @Test
  public void matrices() {
    eval("x <- matrix(1:8, nrow=4)");
    assertThat(eval(".Internal(complete.cases(x))"), elementsIdenticalTo(c(true, true, true, true)));

  }
}
