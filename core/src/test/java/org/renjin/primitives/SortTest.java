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
package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.junit.Assert.assertThat;

public class SortTest extends EvalTestCase {

  @Test
  public void sortInts (){
    assertThat(eval(".Internal(sort(c(1L,5L,2L,9L,1L), decreasing=TRUE))"), elementsIdenticalTo(c_i(9,5,2,1,1)));
    assertThat(eval(".Internal(sort(c(1L,5L,2L,9L,1L), decreasing=FALSE))"), elementsIdenticalTo(c_i(1,1,2,5,9)));
  }

  @Test
  public void sortLogical (){
    assertThat(eval(".Internal(psort(c(TRUE,FALSE,FALSE,TRUE,FALSE), 1:3))"), elementsIdenticalTo(c(false, false, false, true, true)));
  }

  @Test
  public void rank () {
    assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2), \"min\"))"), elementsIdenticalTo(c_i(3, 7, 1, 1, 3, 3, 3)));
    assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2), \"max\"))"), elementsIdenticalTo(c_i(6, 7, 2, 2, 6, 6, 6)));
    assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2), \"average\"))"), elementsIdenticalTo(c(4.5, 7.0, 1.5, 1.5, 4.5, 4.5, 4.5)));
    assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2), \"min\"))"), elementsIdenticalTo(c_i(3, 7, 1, 1, 3, 3, 3)));
    assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2), \"max\"))"), elementsIdenticalTo(c_i(6, 7, 2, 2, 6, 6, 6)));
    assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2), \"average\"))"), elementsIdenticalTo(c(4.5, 7.0, 1.5, 1.5, 4.5, 4.5, 4.5)));
    assertThat(eval(".Internal(rank(c('a','b','a','d','e'), \"min\"))"), elementsIdenticalTo(c_i(1, 3, 1, 4, 5)));
    assertThat(eval(".Internal(rank(c('A','B','A','D','E'), \"min\"))"), elementsIdenticalTo(c_i(1, 3, 1, 4, 5)));
    assertThat(eval(".Internal(rank(c(2L, 3L, 1L, 1L, 2L, 2L, 2L), \"min\"))"), elementsIdenticalTo(c_i(3, 7, 1, 1, 3, 3, 3)));
    assertThat(eval(".Internal(rank(c(2L, 3L, 1L, 1L, 2L, 2L, 2L), \"max\"))"), elementsIdenticalTo(c_i(6, 7, 2, 2, 6, 6, 6)));
    assertThat(eval(".Internal(rank(c(2L, 3L, 1L, 1L, 2L, 2L, 2L), \"average\"))"), elementsIdenticalTo(c(4.5, 7.0, 1.5, 1.5, 4.5, 4.5, 4.5)));
    assertThat(eval(".Internal(rank(integer(0), \"min\"))"), elementsIdenticalTo(c_i()));
    assertThat(eval(".Internal(rank(integer(0), \"max\"))"), elementsIdenticalTo(c_i()));
    assertThat(eval(".Internal(rank(integer(0), \"average\"))"), elementsIdenticalTo(c(new double[0])));

    assertThat(eval(".Internal(rank(c(2, 3, 1, 1, 2, 2, 2, 3), \"max\"))"), elementsIdenticalTo(c_i(6, 8, 2, 2, 6, 6, 6, 8)));
  }


  @Test
  public void sortStringsDescending (){
    assertThat(eval(".Internal(sort(c('5','8','7','50'), decreasing=TRUE))"), elementsIdenticalTo(c("8","7","50","5")));
  }

  @Test
  public void sortNumericsDescending(){
        /* Needs to be implemented */
  }

  @Test
  public void testWhichMin() {
    assertThat (eval("which.min(c(6,5,4,6,5,4,1))"), elementsIdenticalTo(c_i(7)));
    assertThat (eval("which.min(c())"), elementsIdenticalTo(c_i()));
    assertThat (eval("which.min(c(6,5,4,6,5,4,1))"), elementsIdenticalTo(c_i(7)));
    assertThat (eval("which.min(c(NA,6,5,NA,4,6,5,4,1))"), elementsIdenticalTo(c_i(9)));
    assertThat (eval("which.min(c(NA,NA))"), elementsIdenticalTo(c_i()));
  }

  @Test
  public void testWhichMax() {
    assertThat (eval("which.max(c(6,5,4,6,5,4,1,6))"), elementsIdenticalTo(c_i(1)));
    assertThat (eval("which.max(c())"), elementsIdenticalTo(c_i()));
    assertThat (eval("which.max(c(NA,6,5,NA,4,NA,6,NaN,5,4,1,6))"), elementsIdenticalTo(c_i(2)));
    assertThat (eval("which.max(c(NA,NA))"), elementsIdenticalTo(c_i()));
  }

  @Test
  public void orderTest() {
      
        /*  1 2 3
         *  -----
         *  1 1 3  
         *  1 2 9
         *  1 1 1 
         */

    assertThat( eval(".Internal(order(TRUE,FALSE,c(1,1,1), c(1,2,1), c(3,9,1)))"), elementsIdenticalTo(c_i(3,1,2)));
    assertThat( eval(".Internal(order(TRUE,TRUE,c(1,1,1), c(1,2,1), c(3,9,1)))"), elementsIdenticalTo(c_i(2,1,3)));
  }

  @Test
  public void qsort() {
    assertThat( eval(".Internal(qsort(c(3,1,5,0), FALSE))"), elementsIdenticalTo(c(0, 1, 3, 5)));
  }

  @Test
  public void unsorted() {
    assertThat( eval(".Internal(is.unsorted(c(1,2,3), TRUE))"), elementsIdenticalTo(c(false)) );
    assertThat( eval(".Internal(is.unsorted(c(3,2,1), FALSE))"), elementsIdenticalTo(c(true)) );
    assertThat( eval(".Internal(is.unsorted(c(1,1,1), TRUE))"), elementsIdenticalTo(c(true)) );
    assertThat( eval(".Internal(is.unsorted(c(1,1,1), FALSE))"), elementsIdenticalTo(c(false)) );
  }

  @Test
  public void whichMaxRetainsNames() {
    eval("x <- which.max(c(a=1,b=2,c=3))");
    assertThat(eval("names(x)"), elementsIdenticalTo(c("c")));

    eval("x <- which.min(c(a=1,b=2,c=3))");
    assertThat(eval("names(x)"), elementsIdenticalTo(c("a")));
  }
}
