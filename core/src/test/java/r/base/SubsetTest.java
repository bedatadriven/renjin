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

package r.base;

import org.hamcrest.Matchers;
import org.junit.Test;
import r.EvalTestCase;
import r.lang.*;
import r.lang.exception.EvalException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class SubsetTest extends EvalTestCase {

  @Test
  public void subsetDouble() {
    eval( " x <- c(91,92,93) ");
    assertThat( eval(" x[1] "), equalTo( c(91) ));
    assertThat( eval(" x[2] "), equalTo( c(92) ));
    assertThat( eval(" x[3] "), equalTo( c(93) ));
    assertThat( eval(" x[4] "), equalTo( c(DoubleVector.NA)) );
    assertThat( eval(" x[0] "), equalTo( (SEXP) new DoubleVector() ));
    assertThat( eval(" x[3L] "), equalTo( c(93) ));
  }

  @Test
  public void subsetWithLogicals() {
    eval( " x <- c(91,92,93) ") ;
    assertThat( eval("x[c(TRUE,FALSE,TRUE)]"),equalTo( c(91,93)));
  }


  @Test
  public void listIndices() {
    eval(" x <- list('a', 3, NULL) ");

    assertThat( eval("x[0] "), equalTo( list() ));
    assertThat( eval("x[1] "), equalTo( list( c("a") )));
    assertThat( eval("x[99] "), equalTo( list( NULL )));
    assertThat( eval("x[1:2] "), equalTo( list( c("a"), c(3) )));
    assertThat( eval("x[2:5] "), equalTo( list( c(3), NULL, NULL, NULL ) ));
    assertThat( eval("x[-3] "), equalTo( list( c("a"), c(3) )));
  }

  @Test
  public void subsetDoubleMultipleIndices() {
    eval( " x <- c(91,92,93) ");
    assertThat( eval(" x[2:3] "), equalTo( c(92,93) ));
    assertThat( eval(" x[3:5] "), equalTo( c(93, DoubleVector.NA, DoubleVector.NA) ));
  }

  @Test
  public void stringSubset() {
    eval(" x <- c('a','b','c') ");

    assertThat( eval("x[0] "), equalTo( CHARACTER_0 ));
    assertThat( eval("x[1] "), equalTo( c("a") ));
    assertThat( eval("x[99] "), equalTo( c( StringVector.NA )));
    assertThat( eval("x[1:2] "), equalTo( c("a", "b") ));
    assertThat( eval("x[2:5] "), equalTo( c("b", "c", StringVector.NA, StringVector.NA )));
    assertThat( eval("x[-3] "), equalTo( c("a", "b")));
  }

  @Test
  public void stringSubsetAssign() {
    eval(" x <- c('a', 'b', 'c') ");
    eval(" x[1] <- 'z' ");

    assertThat( eval(" x "), equalTo( c("z", "b", "c")));
  }

  @Test
  public void assignNarrower() {
    eval(" x <- c('a', 'b', 'c') ");
    eval(" x[4] <- 36 ");

    assertThat( eval(" x "), equalTo( c("a", "b", "c", "36")));
  }

  @Test
  public void assignWider() {
    eval(" x <- c(1,2,3) ");
    eval(" x[2] <- c('foo') ");

    assertThat( eval(" x "), equalTo( c("1", "foo", "3")));
  }

  @Test
  public void negativeIndices() {
    eval(" x <- c(91,92,93)  ");
    assertThat( eval(" x[-1] "), equalTo( c(92,93)));
    assertThat( eval(" x[-1:-2] "), equalTo( c(93)));
    assertThat( eval(" x[c(-2,-241)] "), equalTo( c(91,93)));
    assertThat( eval(" x[c(-1,0,0)] "), equalTo( c(92,93)));

  }

  @Test(expected = EvalException.class)
  public void mixedNegativeAndPos() {
    eval(" x <- c(91,92) ");
    eval(" x[-1,4] ");
  }

  @Test
  public void setDoubleSubset() {
    eval(" x <- c(91, 92, 93) ");
    eval(" x[1] <- 44 ");

    assertThat( eval("x"), equalTo( c(44,92,93 )));
  }

  @Test
  public void logicalIndices() {
    eval("x <- c(21,22,23) ");

    assertThat( eval(" x[TRUE] "), equalTo( c(21,22,23)));
    assertThat( eval(" x[FALSE] "), equalTo( DOUBLE_0 ));
    assertThat( eval(" x[NA] "), equalTo( c(DoubleVector.NA, DoubleVector.NA, DoubleVector.NA) ));
    assertThat( eval(" x[c(TRUE,FALSE,TRUE)] "), equalTo( c(21, 23) ));
    assertThat( eval(" x[c(TRUE,FALSE)] "), equalTo( c(21, 23) ));
  }

  @Test
  public void missingSubscript() {
    eval("x <- 41:43");

    assertThat( eval(" x[] "), equalTo( c_i(41,42,43)));
  }

  @Test
  public void namedSubscripts() {
    eval("x <- c(a=3, b=4) ");

    assertThat( eval(" x['a'] "),equalTo( c(3) ));
    assertThat( eval(" names(x['a']) "), equalTo( c( "a" )));
  }

  @Test
  public void namesPreservedCorrectly() {
    eval("x <- c(a=3, 99, b=4) ");

    assertThat( eval(" names(x[c(1,2,NA)]) "), equalTo( c( "a", "", StringVector.NA)));
  }

  @Test
  public void setDoubleRange() {
    eval(" x <- c(91, 92, 93) ");
    eval(" x[1:2] <- c(81,82) ");

    assertThat( eval("x"), equalTo( c( 81, 82, 93 )));
  }

  @Test
  public void setWithLogicalSubscripts() {
    eval(" x <- 1:3 ");
    eval(" x[c(FALSE,TRUE,FALSE)] <- 99");

    assertThat( eval("x"), equalTo( c(1,99,3)));
  }


  @Test
  public void setWithLogicalSubscripts2() {
    eval(" x <- 1:4 ");
    eval(" x[c(FALSE,TRUE)] <- c(91,92)");

    assertThat( eval("x"), equalTo( c(1,91,3,92)));
  }


  @Test
  public void setDoubleRangeMultiple() {
    eval(" x <- c(91, 92, 93) ");
    eval(" x[2:3] <- 63 ");

    assertThat( eval("x"), equalTo( c( 91, 63, 63 )));
  }

  @Test
  public void setDoubleRangeMultipleNewLength() {
    eval(" x <- c(91, 92, 93) ");
    eval(" x[2:5] <- 63 ");

    assertThat( eval("x"), equalTo( c( 91, 63, 63, 63, 63 )));
  }

  @Test
  public void subsetOfPosAndZeroIndices() {
    eval("  x<-c(91, 92, 93, 94, 95) ");

    assertThat( eval("x[c(1,0,1)]"), Matchers.equalTo((SEXP) new DoubleVector(91, 91)));
  }

  @Test
  public void setNoElements() {
    eval(" x<- c(1,2,3) ");
    eval("x[FALSE]<-c()");

    assertThat( eval("x") , equalTo(c(1,2,3)));
  }

  @Test
  public void listElementByName() {
    eval(" p <- list(x=33, y=44) ");

    assertThat( eval("p$x"), equalTo( c(33) ));
  }

  @Test
  public void setListElementByName() {
    eval(" p <- list( x = 44 ) ");
    eval(" p$x <- 88 ");

    assertThat( eval(" p$x "), equalTo( c(88) ));
  }

  @Test
  public void setNewListElementByName() {
    eval(" p <- list( x = 22, y = 33 ) ");
    eval(" p$z <- 44 ");

    assertThat( eval(" p$x "), equalTo( c(22) ));
    assertThat( eval(" p$y "), equalTo( c(33) ));
    assertThat( eval(" p$z "), equalTo( c(44) ));
  }

  @Test
  public void partialListMatch() {
    eval(" x <- list(alligator=33, aardvark=44) ");

    assertThat( eval("x$a"), equalTo( NULL ));
    assertThat( eval("x$all"), equalTo( c(33) ));
  }

  @Test
  public void pairListPartial() {

    PairList list = PairList.Node.newBuilder()
        .add(symbol("alligator"), c(1))
        .add(symbol("aardvark"), c(3))
        .build();

    SEXP result = Subset.getElementByName(list, new Symbol("all"));
    assertThat(result, equalTo((SEXP)c(1)));
  }

  @Test(expected = EvalException.class)
  public void listIndexOutOfBounds() {
    eval(" x <- list(1,2) ");
    eval(" x[[3]] ");
  }

  @Test
  public void indexOnNull() {
    eval(" x<- NULL ");
    assertThat( eval("x[[1]]"), equalTo(NULL));
  }

  @Test
  public void columnIndex() {
    eval(" x <- 1:8 ");
    eval(" dim(x) <- c(4,2) ");

    assertThat( eval("x[,2]"), equalTo( c_i(5,6,7, 8) ));
    assertThat( eval("dim(x[,2])"), equalTo( NULL ));
    assertThat( eval("dim(x[,2,drop=TRUE])"), equalTo( NULL ));
    assertThat( eval("dim(x[,2,drop=FALSE])"), equalTo( c_i(4, 1) ));
  }

  @Test
  public void rows() {
    eval(" x <- 1:8 ");
    eval(" dim(x) <- c(4,2) ");

    assertThat( eval("x[3:4,]"), equalTo( c_i(3,4,7,8) ));
    assertThat( eval("dim(x[3:4,])"), equalTo( c_i(2,2) ));
  }

  @Test
  public void byNamedCol() {
    eval( " x <- .Internal(rbind(1, c(a=1,b=2))) ");

    assertThat( eval(" x[,'b'] "), equalTo( c(2) ));
  }

  @Test
  public void arrayDimsCorrectlyPreserved() {
    eval(" x<- 1:8 ");
    eval(" dim(x) <- 8");

    assertThat( eval(" dim(x[1:4]) "), equalTo( c_i(4) ));
    assertThat( eval(" dim(x[1]) "), equalTo( NULL ));
    assertThat( eval(" dim(x[1,drop=FALSE]) "), equalTo( c_i(1) ));
  }

  @Test
  public void subscriptsOnNull() {
    eval(" x <- NULL ");

    assertThat( eval(" x[1] "), equalTo( NULL ));
    assertThat( eval(" x[c(TRUE,FALSE)] "), equalTo( NULL ));
    assertThat( eval(" x[c(1,2,3)] "), equalTo( NULL ));
    assertThat( eval(" x[-1] "), equalTo( NULL ));
    assertThat( eval(" x[] "), equalTo( NULL ));
  }

  @Test
  public void integerIndex() {
    eval(" x<- FALSE ");
    assertThat( eval(" x[1L] "), equalTo( c(false)));
  }

  @Test
  public void replaceListItem() {
    eval(" x<- list(91, 'foo', NULL) ");
    eval(" x[[3]] <- 41 ");

    assertThat( eval("x"), equalTo( list(91d, "foo", 41d)) );
  }

  @Test
  public void replaceVectorItemWithWidening() {
    eval(" x<- c(91,92) ");
    eval(" x[[2]] <- 'foo' ");

    assertThat( eval("x"), equalTo( c("91", "foo")) );
  }

}