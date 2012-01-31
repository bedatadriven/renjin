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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import r.EvalTestCase;
import r.lang.Logical;
import r.lang.SEXP;

public class CombineTest extends EvalTestCase {

    @Test
    public void realList() {
        assertThat(eval("c(1,2,3)"), equalTo(c(1, 2, 3)));
    }

    @Test
    public void logicals() {
        assertThat(eval("c(TRUE, FALSE, NA)"), equalTo(c(Logical.TRUE, Logical.FALSE, Logical.NA)));
    }

    @Test
    public void ints() {
        assertThat(eval("c(1L,2L, 3L) "), equalTo(c_i(1, 2, 3)));
    }

    @Test
    public void nullValues() {
        assertThat(eval("c(NULL, NULL)"), equalTo((SEXP) NULL));
    }

    @Test
    public void realAndLogicalsMixed() {
        assertThat(eval("c(1,2,NULL,FALSE)"), equalTo(c(1, 2, 0)));
    }

    @Test
    public void twoLists() {
        assertThat(eval("c( list(1,2), list(3,4) ) "), equalTo(list(1d, 2d, 3d, 4d)));
    }

    @Test
    public void nullsInList() {
        assertThat(eval("c( list(NULL), NULL, list(NULL,1) ) "),
                equalTo(list(NULL, NULL, 1d)));
    }

    @Test
    public void combineWithExplicitNames() {
        eval("p <- c(x=41,y=42)");

        assertThat(eval("p['x']"), equalTo(c(41)));
    }

    @Test
    public void combineWithExistingNames() {
        eval("x <- c(a=1, b=2, 3)");
        eval("y <- c(x, zz=x, 4)");

        assertThat(eval("names(y)"), equalTo(c("a", "b", "", "zz.a", "zz.b", "zz3", "")));
    }

    @Test
    public void unlistAtomic() {
        assertThat(eval(".Internal(unlist( list(1,4,5), TRUE, TRUE )) "), equalTo(c(1, 4, 5)));
        assertThat(eval(".Internal(unlist( list(1,'a',TRUE), TRUE, TRUE )) "), equalTo(c("1", "a", "TRUE")));
        assertThat(eval(".Internal(unlist( list(1,globalenv()), TRUE, TRUE )) "),
                equalTo(list(1d, global)));
    }

    @Test
    public void combineRecursively() {
        assertThat(eval("c( list(91,92,c(93,94,95)), 96, c(97,98), recursive=TRUE)"),
                equalTo(c(91, 92, 93, 94, 95, 96, 97, 98)));
    }

    @Test
    public void combineRecursivelyWithNames() {
        eval(" x <- c(a=91,92,c=93)");
        eval(" y <- c(recursive=TRUE, A=list(p=x,q=x,list(r=3,s=c(1,2,3,4))),B=4,C=x)");

        assertThat(eval(" names(y) "), equalTo(c("A.p.a", "A.p2", "A.p.c", "A.q.a", "A.q2", "A.q.c", "A.r",
                "A.s1", "A.s2", "A.s3", "A.s4", "B", "C.a", "C2", "C.c")));
    }

    @Test
    public void pairList() {
        eval(" pairlist <- function(...) .Internal(as.vector(list(...), 'pairlist')) ");
        eval(" x <- c(pairlist(x=91,y=92)) ");

        assertThat(eval("names(x)"), equalTo(c("x", "y")));
        assertThat(eval("length(x)"), equalTo(c_i(2)));
        assertThat(eval(".Internal(typeof(x))"), equalTo(c("list")));
        assertThat(eval("x[[1]]"), equalTo(c(91)));
        assertThat(eval("x[[2]]"), equalTo(c(92)));
    }

    @Test
    public void rbindSimple() {

        eval(" x<-.Internal(rbind(1, c(Package='survey', Version='3.22-3'))) ");

        assertThat(eval("dim(x)"), equalTo(c_i(1, 2)));
        assertThat(eval("dimnames(x)"), equalTo(list(NULL, c("Package", "Version"))));
        assertThat(eval("x"), equalTo(c("survey", "3.22-3")));
    }

    @Test
    public void cbind() {

        assertThat(eval(".Internal(cbind(1))"), equalTo(NULL));
        assertThat(eval(".Internal(cbind(1, 5, 6, 7))"), equalTo(c(5, 6, 7)));
        assertThat(eval("dim(.Internal(cbind(1, 5,6, 7)))"), equalTo(c_i(1, 3)));
        assertThat(eval(".Internal(cbind(1, c(5,6), c(9)))"), equalTo(c(5, 6, 9, 9)));
    }
    
    @Test
    public void bindWithEmpty() {
        eval("x<-1:12");
        eval("dim(x)<-c(3,4)");
        
        assertThat(eval("dim(.Internal(cbind(1, x, c())))"), equalTo(c_i(3,4)));
        assertThat(eval("dim(.Internal(rbind(1, x, c())))"), equalTo(c_i(3,4)));

        assertThat(eval("dim(.Internal(cbind(1, c())))"), equalTo(NULL));
        assertThat(eval("dim(.Internal(rbind(1, c())))"), equalTo(NULL));
        
    }

    @Test
    public void aperm() {
        // from docs
        eval("x <- 1:24");
        eval("dim(x) <- 2:4 ");
        eval("xt <- .Internal(aperm(x, c(2,1,3), TRUE)) ");

        assertThat(eval("xt"), equalTo(c_i(1, 3, 5, 2, 4, 6, 7, 9, 11, 8, 10, 12, 13, 15, 17, 14, 16, 18, 19, 21, 23, 20, 22, 24)));
    }

    @Test
    public void matrix() {
        assertThat(eval(".Internal(matrix(c(1,2,3,4),2,2,FALSE,NULL))"), equalTo(c(1, 2, 3, 4)));
        assertThat(eval(".Internal(matrix(c(1,2,3,4),2,4,FALSE,NULL))"), equalTo(c(1, 2, 3, 4, 1, 2, 3, 4)));
        assertThat(eval("as.double(.Internal(matrix(1:10,5,2,FALSE,NULL)))"), equalTo(c(1,2,3,4,5,6,7,8,9,10)));
    }

    @Test
    public void matrixByRow() {
        assertThat(eval(".Internal(matrix(c(1,2,3,4),2,2,TRUE,NULL))"), equalTo(c(1, 3, 2, 4)));
        assertThat(eval(".Internal(matrix(c(1,2,3,4),2,4,TRUE,NULL))"), equalTo(c(1, 1, 2, 2, 3, 3, 4, 4)));
        assertThat(eval("as.double(.Internal(matrix(1:10,5,2,TRUE,NULL)))"), equalTo(c(1,3,5,7,9,2,4,6,8,10)));
    }
    
  @Test
  public void rowTest() {
    assertThat(eval(".Internal(row(dim(.Internal(matrix(1:12,3,4, FALSE,NULL)))))"), equalTo(c_i(1,2,3,1,2,3,1,2,3,1,2,3)));
  }
  
  @Test
  public void colTest() {
    assertThat(eval(".Internal(col(dim(.Internal(matrix(1:12,3,4, FALSE,NULL)))))"), equalTo(c_i(1,1,1,2,2,2,3,3,3,4,4,4)));
  }
  
  @Test
  public void bindDispatch() {
    eval("rbind.foo <- function(..., deparse.level = 1) 42L ");
    eval("rbind.bar <- function(..., deparse.level = 1) c(...)*2 ");

    eval("x <- 1");
    eval("class(x) <- 'foo'");
    
    eval("y <- 2");
    
    eval("z <- 3");
    eval("class(z) <- 'bar'");
    
    assertThat(eval(".Internal(rbind(1, x, y))"), equalTo(c_i(42)));
    assertThat(eval(".Internal(rbind(1, y, x))"), equalTo(c_i(42)));
    
    assertThat(eval(".Internal(rbind(1, x, y, z))"), equalTo(c(1,2,3))); // default method
    
    assertThat(eval(".Internal(rbind(1, y, z))"), equalTo(c(4, 6))); // default method
    
  }
  
}
