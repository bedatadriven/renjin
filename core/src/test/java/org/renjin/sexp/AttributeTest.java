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
  
}
