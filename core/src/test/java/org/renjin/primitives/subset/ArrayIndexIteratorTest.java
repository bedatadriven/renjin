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
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.LogicalArrayVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.renjin.primitives.subset.IndexIterator.EOF;

public class ArrayIndexIteratorTest {

  @Test
  public void matrix() {
    
    // 3 x 4 matrix
    int dim[] = new int[] { 3, 4 };
    
    // x[,]
    Subscript[] subscripts = {new MissingSubscript(3), new MissingSubscript(4)};

    ArrayIndexIterator it = new ArrayIndexIterator(dim, subscripts);
    
    assertThat(it.next(), equalTo(0));
    assertThat(it.next(), equalTo(1));
    assertThat(it.next(), equalTo(2));
    assertThat(it.next(), equalTo(3));
    assertThat(it.next(), equalTo(4));
    assertThat(it.next(), equalTo(5));
    assertThat(it.next(), equalTo(6));
    assertThat(it.next(), equalTo(7));
    assertThat(it.next(), equalTo(8));
    assertThat(it.next(), equalTo(9));
    assertThat(it.next(), equalTo(10));
    assertThat(it.next(), equalTo(11));
    assertThat(it.next(), equalTo(EOF));
  }

  @Test
  public void array3() {

    // 2 x 3 x 4 array
    int dim[] = new int[] { 2, 3, 4 };

    // x[1,1:2,]
    Subscript[] subscripts = {
        new IndexSubscript(new IntArrayVector(1), dim[0]), 
        new IndexSubscript(new IntArrayVector(1, 2), dim[1]),
        new MissingSubscript(dim[2]) };
    
    ArrayIndexIterator it = new ArrayIndexIterator(dim, subscripts);

    assertThat(it.next(), equalTo(0));
    assertThat(it.next(), equalTo(2));
    assertThat(it.next(), equalTo(6));
    assertThat(it.next(), equalTo(8));
    assertThat(it.next(), equalTo(12));
    assertThat(it.next(), equalTo(14));
    assertThat(it.next(), equalTo(18));
    assertThat(it.next(), equalTo(20));
    assertThat(it.next(), equalTo(EOF));
  }
  
  @Test
  public void array1() {


    // 2 x 3 x 4 array
    int dim[] = new int[] { 8 };

    // x[3:4]
    Subscript[] subscripts = { new IndexSubscript(new IntArrayVector(3,4), dim[0]) };
    
    ArrayIndexIterator it = new ArrayIndexIterator(dim, subscripts);
    assertThat(it.next(), equalTo(2));
    assertThat(it.next(), equalTo(3));
    assertThat(it.next(), equalTo(EOF));
    
  }
  
  @Test
  public void naIndexes() {
    
    int dim[] = new int[] { 1, 1, 4 };
    
    Subscript[] subscripts = {
        new MissingSubscript(dim[0]),
        new MissingSubscript(dim[1]), 
        new LogicalSubscript(new LogicalArrayVector(IntVector.NA), dim[2])
    };
    
    ArrayIndexIterator it = new ArrayIndexIterator(dim, subscripts);
    assertThat(it.next(), equalTo(IntVector.NA));
    assertThat(it.next(), equalTo(IntVector.NA));
    assertThat(it.next(), equalTo(IntVector.NA));
    assertThat(it.next(), equalTo(IntVector.NA));
    assertThat(it.next(), equalTo(EOF));    
    
  }
  
}