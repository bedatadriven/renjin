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
package org.renjin.stats.internals;

import org.junit.Test;
import org.renjin.EvalTestCase;

import java.io.IOException;

import static org.junit.Assert.assertThat;

public class SamplingTest extends EvalTestCase {

  @Test
  public void withReplacement() throws IOException {
    double delta = 0.00001;


    eval("x<-1:5");
    eval("p<-c(0.00, 0.00, 0.00, 1, 0.00)");

    assertThat(eval("sample(x,1L,TRUE,p)").asReal(), closeTo(4, delta));
  }

  @Test
  public void withReplacementUniform() throws IOException {
    double delta = 0.1;

    eval("x<-1:5");
    assertThat(eval("mean(sample(x, 10000L, TRUE, rep(1/5,5)))").asReal(), closeTo(3.0, delta));
  }

  @Test
  public void withoutReplacement() throws IOException {

    eval("x<-c(1,2,3,4,5,10,9,8,7,6)");
    assertThat(eval("sort(sample(x, 10L, FALSE))"), elementsIdenticalTo(c(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
  }

  @Test
  public void minimumParametersCall() throws IOException {

    eval("x<-c(1,2,3,4,5,10,9,8,7,6)");
    assertThat(eval("sort(sample(x, 10L))"), elementsIdenticalTo(c(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
  }

  @Test
  public void testWithoutReplacement() throws IOException {

    eval("set.seed(12345);");
    assertThat(eval("sample(c(1,2,3,4,5,6,7,8,9,10), 10)"), elementsIdenticalTo(c(8, 10, 7, 9, 3, 1, 2, 4, 6, 5)));
  }

  @Test
  public void testWithReplacement() throws IOException {

    eval("set.seed(12345);");
    assertThat(eval("sample(c(1,2,3,4,5,6,7,8,9,10), replace = T)"), elementsIdenticalTo(c(8, 9, 8, 9, 5, 2, 4, 6, 8, 10)));
  }

  @Test
  public void testWithProb() throws IOException {

    eval("set.seed(12345);");
    assertThat(eval("sample(c(1,2,3,4), prob = c(0.1, 0.15, 0.25, 0.50))"), elementsIdenticalTo(c(3, 1, 4, 2)));
  }

  @Test
  public void testWithWeights() throws IOException {

    eval("set.seed(12345);");
    assertThat(eval("sample(c(1,2,3,4), prob = c(1, 100, 10, 7))"), elementsIdenticalTo(c(2,4,3,1)));
  }

  @Test
  public void testSmallSample() throws IOException {

    eval("set.seed(12345);");
    assertThat(eval("sample(c(20,30,40,38,27,29,32,100,24), 5)"), elementsIdenticalTo(c(32, 100, 29, 24, 40)));
  }

  @Test
  public void sampleWithPresortedProbs() throws  IOException {

    eval("set.seed(12345)");
    assertThat(eval("as.double(sample(0:4, size = 1, prob = c(0.2356849, 0.2163148, 0.1985367, 0.1822197, 0.1672438)))"), elementsIdenticalTo(c(3L)));
  }
  
  @Test
  public void uniformSampleWithReplacement() {
    eval("set.seed(8024)");
    eval("n <- 10e6");
    eval("m <- 1e6");
    eval("x <- sample(m, n, replace=TRUE)");
    
    // Expected result from R 3.2.0
    assertThat(eval("x[1:5]"), elementsIdenticalTo(c_i(855610, 258712, 357515, 584505, 949772)));
  }
}
