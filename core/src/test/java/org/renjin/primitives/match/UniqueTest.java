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
package org.renjin.primitives.match;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class UniqueTest extends EvalTestCase {

  @Test
  public void atomicVectors() {
    assertThat( eval(".Internal(unique(c(1,3,1,4,4), FALSE, FALSE))"), equalTo( c(1,3,4)) );
  }
  
  @Test
  public void fromLast() {
    assertThat( eval(".Internal(unique(c(1,3,1,4,4), FALSE, TRUE))"), equalTo( c(3,1,4)) );    
  }

  @Test
  public void uniqueInt() {
    assertThat( eval(" .Internal(unique(1L, FALSE, FALSE)) "), CoreMatchers.equalTo(c_i(1)));
  }
  
  @Test
  public void falseIncomparablesIsTreatedAsNull() {
    assertThat( eval(" .Internal(unique(c(0, 1, 0, 0, 0, 0, 0, 0), FALSE, FALSE))"), equalTo(c(0,1)));
  }
  
  @Test
  public void uniqueList() {
    assertThat( eval(" .Internal(unique(list('a','b','a','a'), FALSE,FALSE))"), equalTo(list("a","b")));
    assertThat( eval(" .Internal(unique(list('a','a', c(1,2), c(1,2)), FALSE,FALSE))"), 
        equalTo(list("a",c(1,2))));
    assertThat( eval(" length(unique(list('a','a', c(1,2), c(a=1,b=2))))"),
        equalTo(c_i(3)));
    

  }

}
