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