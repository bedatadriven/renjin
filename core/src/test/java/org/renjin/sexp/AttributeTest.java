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

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.EvalException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class AttributeTest extends EvalTestCase {

  @Test
  public void listWithAttributes() {

    eval( "p <- list(x=1,y=3) ");

    assertThat( eval("p$x"), elementsIdenticalTo(c(1)));
  }
  
  @Test
  public void noAttributesIsNull() {
    assertThat( eval("attributes(1)"), identicalTo((SEXP)Null.INSTANCE));
  }
  
  @Test
  public void arrayNamesDropsNames() {
    eval("x <- c(a=1,b=2,c=3)");
    eval("dim(x) <- 3L");
    assertThat(eval("names(x)"), identicalTo(NULL));
    assertThat(eval("length(attributes(x))"), elementsIdenticalTo(c_i(1)));
  }
  
  @Test
  public void arrayNames() {
    eval("x <- c(1,2,3)");
    eval("dim(x) <- 3L");
    eval("dimnames(x)[[1]] <- c('a','b','c')");
    
    assertThat(eval("names(x)"), elementsIdenticalTo(c("a", "b", "c")));
  }

  @Test
  public void attrExact() {
    eval("x <- c(1,2,3)");
    eval("attr(x, 'foo') <- 'bar' ");
    
    assertThat(eval("attr(x, 'foo', exact=TRUE)"), elementsIdenticalTo(c("bar")));
    assertThat(eval("attr(x, 'foo', exact=FALSE)"), elementsIdenticalTo(c("bar")));
    assertThat(eval("attr(x, 'f', exact=FALSE)"), elementsIdenticalTo(c("bar")));
    assertThat(eval("attr(x, 'f', exact=TRUE)"), identicalTo(NULL));
  }
  
  @Test
  public void attributesWithNullCastToList() {
    eval("x <- NULL");
    eval("attributes(x) <- list(class='foo')");
    
    SEXP x = eval("x");
    assertThat(x, instanceOf(ListVector.class));
    assertThat(x.getAttributes().getClassVector(), elementsIdenticalTo(c("foo")));
  }
  
  @Test(expected = EvalException.class)
  public void attributesWithNull() {
    eval("attributes(NULL) <- list(class='x')");
  }
  
  @Test
  public void naName() {
    eval("x <- 1:2");
    eval("names(x) <- c('A', NA) ");
    
    assertThat(eval("names(x)"), elementsIdenticalTo(c("A", null)));
    
    eval("y <- c(x)");
    assertThat(eval("names(y)"), elementsIdenticalTo(c("A", null)));

    eval("z <- c(`NA`=x)");
    assertThat(eval("names(z)"), elementsIdenticalTo(c("NA.A", "NA.NA")));
  }

  @Test
  public void zeroLengthDimNameIsConvertedToNull() {
    eval("x <- matrix(1:12, nrow=3)");
    eval("dimnames(x) <- list(character(0), letters[1:4])");
    
    assertThat(eval("dimnames(x)[[1]]"), identicalTo((SEXP) Null.INSTANCE));
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

    assertThat(eval("dimnames(m)"), identicalTo((SEXP) Null.INSTANCE));
  }

  @Test
  public void settingDimsDropDimnamesEvenIfThereIsNoChange() {
    eval("m <- matrix(1:3, nrow=3)");
    eval("dimnames(m) <- list(letters[1:3], 'X')");

    eval("dim(m) <- c(3,1)");

    assertThat(eval("dimnames(m)"), identicalTo((SEXP) Null.INSTANCE));
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
    
    assertThat(eval("names(z)"), elementsIdenticalTo(c("a", "b", "c")));
  }

  @Test
  public void dimBeatsNames() {
    eval("x <- c(a=1,b=2,c=3,d=4)");
    eval("y <- matrix(1:4, nrow=2)");
    eval("z <- x + y");
    
    assertThat(eval("dim(z)"), elementsIdenticalTo(c_i(2, 2)));
    assertThat(eval("names(z)"), identicalTo(NULL));
  }
  
  @Test
  public void dimLabels() {
    eval("x <- matrix(1:4, nrow=2)");
    eval("dimnames(x) <- list(a=c('X','Y'), b=c('A','B'))");
    
    assertThat(eval("names(dimnames(x))"), elementsIdenticalTo(c("a", "b")));
  }
  
  @Test
  public void logicalAndCombineNamesOnly() {
    // Some operators lke '&' or '|' include only the 
    // dim, dimnames, and names attributes from the operands
    
    eval("x <- c(a=TRUE, b=TRUE)");
    eval("class(x) <- 'foo'");
    eval("y <- c(x=TRUE, y=FALSE)");
    eval("z <- x & y");
    
    assertThat(eval("names(z)"), elementsIdenticalTo(c("a", "b")));
    assertThat(eval("is.null(attr(z, 'class'))"), elementsIdenticalTo(c(true)));
  }
  
  @Test
  public void whenCombiningAttributesDimTakePrecedence() {

    eval("x <- c(a=TRUE, b=FALSE)");
    eval("y <- matrix(TRUE, nrow=2, ncol=2)");
    eval("z <- x | y");
    
    assertThat(eval("dim(z)"), elementsIdenticalTo(c_i(2, 2)));
    assertThat(eval("is.null(names(z))"), elementsIdenticalTo(c(true)));
  }
 
  @Test
  public void settingNullNamesDoesNotClearDimsAttributes() {
    eval("x <- matrix(1:12, nrow=3) ");
    eval("names(x) <- NULL");
    
    assertThat(eval("dim(x)"), elementsIdenticalTo(c_i(3, 4)));
  }
  
  @Test
  public void dimsThenNamesAllowed() {
    
    eval("x <- matrix(1:12, nrow=3)");
    eval("names(x) <- letters[1:12] ");
    
    assertThat(eval("dim(x)"), elementsIdenticalTo(c_i(3, 4)));
    assertThat(eval("names(x)"), elementsIdenticalTo(c("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l")));
  }

  @Test
  public void namesThenDimsDropsNames() {

    eval("x <- 1:12");
    eval("names(x) <- letters[1:12] ");
    eval("dim(x) <- c(3, 4)");

    assertThat(eval("dim(x)"), elementsIdenticalTo(c_i(3, 4)));
    assertThat(eval("names(x)"), identicalTo(NULL));
  }
  
  @Test
  public void setNamesInvokesAsCharacter() {
    eval("x <- 1:3");
    eval("y <- 1:3");
    eval("class(y) <- 'foo'");
    eval("as.character.foo <- function(y) letters[y] ");
    
    eval("names(x) <- y");
    assertThat(eval("names(x)"), elementsIdenticalTo(c("a", "b", "c")));
  }

  @Test
  public void setAttrNamesDoesNotInvokesAsCharacter() {
    eval("x <- 1:3");
    eval("y <- 1:3");
    eval("class(y) <- 'foo'");
    eval("as.character.foo <- function(y) letters[y] ");

    eval("attr(x, 'names') <- y");
    assertThat(eval("names(x)"), elementsIdenticalTo(c("1", "2", "3")));
  }

  @Test
  public void setAttrNamesWithList() {
    eval("x <- 1:3");
    eval("attr(x, 'names') <- list('a', 'b', 'c')");
    assertThat(eval("names(x)"), elementsIdenticalTo(c("a", "b", "c")));

    eval("attr(x, 'names') <- list('a', 'b', 1:3)");
    assertThat(eval("names(x)"), elementsIdenticalTo(c("a", "b", "1:3")));

  }

  @Test
  @Ignore("todo")
  public void setAttrNamesWithNestedList() {
    eval("x <- 1:3");

    // Result in GNU R does not match deparse(list(x=1,y=1))
    eval("attr(x, 'names') <- list('a', 'b', list(x=1,y=1))");
    assertThat(eval("names(x)"), elementsIdenticalTo(c("a", "b", "list(x = 1, y = 1")));
  }
  
  @Test
  public void setEmptyDimNames() {
    eval("x <- 1:12");
    eval("dim(x) <- 3:4");
    eval("dimnames(x) <- list()");
    
    assertThat(eval("dimnames(x)"), identicalTo(NULL));
  }

  @Test
  public void setEmptyDimNamesViaAttr() {
    eval("x <- 1:12");
    eval("dim(x) <- 3:4");
    eval("attr(x, 'dimnames') <- list()");

    assertThat(eval("dimnames(x)"), identicalTo(NULL));
  }
  
  @Test
  public void setOnlyRowNames() {
    eval("x <- 1:12");
    eval("dim(x) <- 3:4");
    eval("dimnames(x) <- list(letters[1:3])");

    assertThat(eval("dimnames(x)"), elementsIdenticalTo(list(c("a", "b", "c"), Null.INSTANCE)));
  }
  
  @Test
  public void setOnlyRowNamesViaAttr() {
    eval("x <- 1:12");
    eval("dim(x) <- 3:4");
    eval("attr(x, 'dimnames') <- list(letters[1:3])");

    assertThat(eval("dimnames(x)"), elementsIdenticalTo(list(c("a", "b", "c"), Null.INSTANCE)));
  }
  
}
