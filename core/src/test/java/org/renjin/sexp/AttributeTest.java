/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.sexp;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.EvalException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class AttributeTest extends EvalTestCase {

  @Test
  public void listWithAttributes() {

    eval( "p <- list(x=1,y=3) ");

    assertThat( eval("p$x"), equalTo(c(1)));
  }
  
  @Test
  public void noAttributesIsNull() {
    assertThat( eval("attributes(1)"), equalTo((SEXP)Null.INSTANCE));
  }
  
  @Test
  public void arrayNamesDropsNames() {
    eval("x <- c(a=1,b=2,c=3)");
    eval("dim(x) <- 3L");
    assertThat(eval("names(x)"), equalTo(NULL));
    assertThat(eval("length(attributes(x))"), equalTo(c_i(1)));
  }
  
  @Test
  public void arrayNames() {
    eval("x <- c(1,2,3)");
    eval("dim(x) <- 3L");
    eval("dimnames(x)[[1]] <- c('a','b','c')");
    
    assertThat(eval("names(x)"), equalTo(c("a", "b", "c")));
  }

  @Test
  public void attrExact() {
    eval("x <- c(1,2,3)");
    eval("attr(x, 'foo') <- 'bar' ");
    
    assertThat(eval("attr(x, 'foo', exact=TRUE)"), equalTo(c("bar")));
    assertThat(eval("attr(x, 'foo', exact=FALSE)"), equalTo(c("bar")));
    assertThat(eval("attr(x, 'f', exact=FALSE)"), equalTo(c("bar")));
    assertThat(eval("attr(x, 'f', exact=TRUE)"), equalTo(NULL));
  }
  
  @Test
  public void attributesWithNullCastToList() {
    eval("x <- NULL");
    eval("attributes(x) <- list(class='foo')");
    
    SEXP x = eval("x");
    assertThat(x, instanceOf(ListVector.class));
    assertThat(x.getAttributes().getClassVector(), equalTo(c("foo")));
  }
  
  @Test(expected = EvalException.class)
  public void attributesWithNull() {
    eval("attributes(NULL) <- list(class='x')");
  }
  
  @Test
  public void naName() {
    eval("x <- 1:2");
    eval("names(x) <- c('A', NA) ");
    
    assertThat(eval("names(x)"), equalTo(c("A", null)));
    
    eval("y <- c(x)");
    assertThat(eval("names(y)"), equalTo(c("A", null)));

    eval("z <- c(`NA`=x)");
    assertThat(eval("names(z)"), equalTo(c("NA.A", "NA.NA")));
  }

  @Test
  public void zeroLengthDimNameIsConvertedToNull() {
    eval("x <- matrix(1:12, nrow=3)");
    eval("dimnames(x) <- list(character(0), letters[1:4])");
    
    assertThat(eval("dimnames(x)[[1]]"), equalTo((SEXP) Null.INSTANCE));
  }
 
  
  @Test
  public void dimNamesInStructure() {
    eval("x <- structure(1:12, .Dim = c(3,4), .Dimnames = list(letters[1:3], NULL))");
  }
  
  @Test
  public void changingDimsDropsDimNames() {
    eval("m <- matrix(1:3, nrow=3)");
    eval("dimnames(m) <- list(letters[1:3], 'X')");
    
    eval("dim(m) <- c(1,3)");

    assertThat(eval("dimnames(m)"), equalTo((SEXP) Null.INSTANCE));
  }

  @Test
  public void settingDimsDropDimnamesEvenIfThereIsNoChange() {
    eval("m <- matrix(1:3, nrow=3)");
    eval("dimnames(m) <- list(letters[1:3], 'X')");

    eval("dim(m) <- c(3,1)");

    assertThat(eval("dimnames(m)"), equalTo((SEXP) Null.INSTANCE));
  }
  
  @Test
  public void namesAreDroppedWhenAddingMatrixToVector() {
    eval("x <- matrix(1:12, nrow = 4)");
    eval("y <- c(a=1,b=2)");
    
    eval("z <- x + y");
  }

  @Test
  public void namesAreDroppedWhenAddingMatrixToEqualLengthVector() {
    eval("x <- matrix(1:4, nrow = 2)");
    eval("y <- c(a=1,b=2,c=3,d=4)");

    eval("z <- x + y");
  }
  
  @Test(expected = EvalException.class)
  public void addingNonConformingMatricesThrowsError() {
    eval("x <- matrix(1:12, nrow=3)");
    eval("y <- matrix(1:12, nrow=4)");
    eval("z <- x + y");
  }

  @Test
  public void attributesFromFirstVectorTakePrecedenceWhenAddingVectorsOfEqualLength() {
    eval("x <- c(a=1,b=2,c=3)");
    eval("y <- c(x=20,y=40,z=50)");
    eval("z <- x + y");
    
    assertThat(eval("names(z)"), equalTo(c("a", "b", "c")));
  }

  @Test
  public void dimBeatsNames() {
    eval("x <- c(a=1,b=2,c=3,d=4)");
    eval("y <- matrix(1:4, nrow=2)");
    eval("z <- x + y");
    
    assertThat(eval("dim(z)"), equalTo(c_i(2, 2)));
    assertThat(eval("names(z)"), equalTo(NULL));
  }
  
  @Test
  public void dimLabels() {
    eval("x <- matrix(1:4, nrow=2)");
    eval("dimnames(x) <- list(a=c('X','Y'), b=c('A','B'))");
    
    assertThat(eval("names(dimnames(x))"), equalTo(c("a", "b")));
  }
  
  @Test
  public void logicalAndCombineNamesOnly() {
    // Some operators lke '&' or '|' include only the 
    // dim, dimnames, and names attributes from the operands
    
    eval("x <- c(a=TRUE, b=TRUE)");
    eval("class(x) <- 'foo'");
    eval("y <- c(x=TRUE, y=FALSE)");
    eval("z <- x & y");
    
    assertThat(eval("names(z)"), equalTo(c("a", "b")));
    assertThat(eval("is.null(attr(z, 'class'))"), equalTo(c(true)));
  }
  
  @Test
  public void whenCombiningAttributesDimTakePrecedence() {

    eval("x <- c(a=TRUE, b=FALSE)");
    eval("y <- matrix(TRUE, nrow=2, ncol=2)");
    eval("z <- x | y");
    
    assertThat(eval("dim(z)"), equalTo(c_i(2, 2)));
    assertThat(eval("is.null(names(z))"), equalTo(c(true)));
  }
  
  
}
