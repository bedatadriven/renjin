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
package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;

import java.io.IOException;

import static org.junit.Assert.assertThat;


public class SplitTest extends EvalTestCase {

  @Test
  public void split() throws IOException {


    eval("x <- split(c(10:1), c(1,2,1,2,1,2,1,2,3,3))");

    assertThat(eval("x$`1`"), elementsIdenticalTo(c_i(10,8,6,4)));
    assertThat(eval("x$`2`"), elementsIdenticalTo(c_i(9,7,5,3)));
    assertThat(eval("x$`3`"), elementsIdenticalTo(c_i(2,1)));
  }
  
  @Test
  public void splitRecycling() {

    eval("x <- split(rep(35,10), 1:2)");
    
    assertThat(eval("length(x)"), elementsIdenticalTo(c_i(2)));
    assertThat(eval("x[[1]]"), elementsIdenticalTo(c(35,35,35,35,35)));
    assertThat(eval("x[[2]]"), elementsIdenticalTo(c(35,35,35,35,35)));
  }
  
  @Test
  public void splitLongerFactor() {

    eval("x <- split(1:2, 1:3)");
    
    assertThat(eval("length(x)"), elementsIdenticalTo(c_i(3)));
    assertThat(eval("x[[1]]"), elementsIdenticalTo(c_i(1)));
    assertThat(eval("x[[2]]"), elementsIdenticalTo(c_i(2)));
    assertThat(eval("x[[3]]"), elementsIdenticalTo(c_i()));
  }
  
  @Test
  public void splitWithMissing() throws IOException {

    
    eval("x <- split(c(10:1), c(1,2,1,2,1,2,1,2,3,NA))");
    eval("print(x)");
    assertThat(eval("x$`1`"), elementsIdenticalTo(c_i(10,8,6,4)));
    assertThat(eval("x$`2`"), elementsIdenticalTo(c_i(9,7,5,3)));
    assertThat(eval("x$`3`"), elementsIdenticalTo(c_i(2)));
  }
  
  @Test
  public void splitWithNames() {

    
    eval("x <- split(c(a=1,b=2), c(1,2))");
    assertThat(eval("names(x)"), elementsIdenticalTo(c("1", "2")));
    assertThat(eval("names(x[[1]])"), elementsIdenticalTo(c("a")));
  }

  @Test
  public void split1dArrayWithNames() {


    eval("a <- 1:2");
    eval("dim(a) <- 2");
    eval("names(a) <- c('x','y')");
    
    eval("x <- split(a, c(1,2))");
    assertThat(eval("names(x)"), elementsIdenticalTo(c("1", "2")));
    assertThat(eval("names(x[[1]])"), elementsIdenticalTo(c("x")));
  }
  
}
