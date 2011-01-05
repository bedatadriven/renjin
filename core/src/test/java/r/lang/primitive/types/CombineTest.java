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

package r.lang.primitive.types;

import org.junit.Test;
import r.lang.EvalTestCase;
import r.lang.Logical;
import r.lang.SEXP;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CombineTest extends EvalTestCase {

  @Test
  public void realList() {
    assertThat( eval("c(1,2,3)"), equalTo( c(1,2,3) ));
  }

  @Test
  public void logicals() {
    assertThat( eval("c(TRUE, FALSE, NA)"), equalTo( c(Logical.TRUE, Logical.FALSE, Logical.NA)) );
  }

  @Test
  public void ints() {
    assertThat( eval("c(1L,2L, 3L) "), equalTo( c_i(1,2,3)));
  }

  @Test
  public void nullValues() {
    assertThat( eval("c(NULL, NULL)"), equalTo( (SEXP) NULL) );
  }

  @Test
  public void realAndLogicalsMixed() {
    assertThat( eval("c(1,2,NULL,FALSE)"), equalTo( c(1,2,0) ));
  }

  @Test
  public void twoLists() {
    assertThat( eval("c( list(1,2), list(3,4) ) "), equalTo( list(1d,2d,3d,4d)));
  }

  @Test
  public void nullsInList() {
    assertThat( eval("c( list(NULL), NULL, list(NULL,1) ) "),
        equalTo( list(NULL, NULL, 1d)));
  }

  @Test
  public void combineWithExplicitNames() {
    eval("p <- c(x=41,y=42)" );

    assertThat( eval("p['x']"), equalTo( c(41) ));
  }

  @Test
  public void combineWithExistingNames() {
    eval("x <- c(a=1, b=2, 3)");
    eval("y <- c(x, zz=x, 4)");

    assertThat( eval("names(y)"), equalTo( c("a","b", "", "zz.a", "zz.b", "zz3", "")) );
  }

  @Test
  public void unlistAtomic() {
    assertThat( eval(".Internal(unlist( list(1,4,5), TRUE, TRUE )) "), equalTo( c(1,4,5)) );
    assertThat( eval(".Internal(unlist( list(1,'a',TRUE), TRUE, TRUE )) "), equalTo( c("1","a","TRUE")) );
    assertThat( eval(".Internal(unlist( list(1,globalenv()), TRUE, TRUE )) "),
        equalTo( list(1d,global)) );
  }

  @Test
  public void combineRecursively() {
    assertThat( eval("c( list(91,92,c(93,94,95)), 96, c(97,98), recursive=TRUE)"),
        equalTo( c(91,92,93,94,95,96,97,98)));
  }

  @Test
  public void combineRecursivelyWithNames() {
    eval(" x <- c(a=91,92,c=93)");
      eval(" y <- c(recursive=TRUE, A=list(p=x,q=x,list(r=3,s=c(1,2,3,4))),B=4,C=x)");

    assertThat( eval(" names(y) "), equalTo( c("A.p.a", "A.p2", "A.p.c", "A.q.a", "A.q2", "A.q.c", "A.r",
        "A.s1", "A.s2", "A.s3", "A.s4", "B", "C.a", "C2", "C.c")));
  }

  @Test
  public void pairList() {
    eval(" pairlist <- function(...) .Internal(as.vector(list(...), 'pairlist')) ");
    eval(" x <- c(pairlist(x=91,y=92))");

    assertThat( eval("length(x)"), equalTo( c_i(2) ));
    assertThat( eval(".Internal(typeof(x))"), equalTo( c("list") ));
    assertThat( eval("x[[1]]"), equalTo( c(91)));
    assertThat( eval("x[[2]]"), equalTo( c(92)));
    assertThat( eval("names(x)"), equalTo( c("x", "y")));
  }

  @Test
  public void rbindSimple() {

    eval(" x<-.Internal(rbind(1, c('survey', '3.22-3'))) ");

    assertThat( eval("dim(x)"), equalTo(c_i(2,2) ));
    assertThat( eval("x"), equalTo(c("1", "survey", "1", "3.22-3")));

  }

}
