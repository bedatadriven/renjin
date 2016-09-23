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
package org.renjin.sexp;

import org.junit.Test;
import org.renjin.primitives.Indexes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class IndexesTest {

  @Test
  public void arrayToVectorIndex() {
    assertThat( Indexes.arrayIndexToVectorIndex(new int[] {0,2}, new int[] {10,3} ), equalTo(20) );
  }

  @Test
  public void increment() {
    assertThat( Indexes.incrementArrayIndex(new int[] {9,0}, new int[] {10,1} ), equalTo(false) );
  }
  
  @Test
  public void vectorToIndex() {
    assertThat( Indexes.arrayIndexToVectorIndex(new int[] {0,2}, new int[] {10,3} ), equalTo(20) );
    
  }

  @Test
  public void vectorIndexToRow() {
    assertThat( Indexes.vectorIndexToRow(0, 3), equalTo(0) );
    assertThat( Indexes.vectorIndexToRow(6, 3), equalTo(0) );
    assertThat( Indexes.vectorIndexToRow(2, 3), equalTo(2) );
    assertThat( Indexes.vectorIndexToRow(5, 3), equalTo(2) );
  }
  
  @Test
  public void vectorIndexToCol() {
    assertThat( Indexes.vectorIndexToCol(0, 3, 4), equalTo(0) );
    assertThat( Indexes.vectorIndexToCol(6, 3, 4), equalTo(2) );
    assertThat( Indexes.vectorIndexToCol(2, 3, 4), equalTo(0) );
    assertThat( Indexes.vectorIndexToCol(5, 3, 4), equalTo(1) );
  }

}
