/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler;

import org.easymock.internal.ThrowableWrapper;
import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class LoopCompilerTest extends EvalTestCase {
  
  @Test
  public void repeatLoop() {
    eval("s <- 0");
    eval("i <- 0");
    eval("repeat { s <- s + i; i <- i+1; if(i > 10000) break; }");
    
    assertThat(eval("s"), elementsIdenticalTo(c(50005000d)));
    assertThat(eval("i"), elementsIdenticalTo(c(10001)));
  }


  @Test
  public void whileLoop() {
    try {
      eval("s <- 0");
      eval("i <- 0");
      eval("while(i <= 10000) { s <- s + i; i <- i+1 }");

      assertThat(eval("s"), elementsIdenticalTo(c(50005000d)));
      assertThat(eval("i"), elementsIdenticalTo(c(10001)));
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}
