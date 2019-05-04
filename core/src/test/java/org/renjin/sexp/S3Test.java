/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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

import org.junit.Before;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.primitives.S3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class S3Test extends EvalTestCase {

  @Before
  public void declareWrappers() {
    eval( "inherits <- function(x, what, which=FALSE) { .Internal(inherits(x,what,which)) } ");
  }

  @Test
  public void implicitClasses() {

    eval(  "x <- 10" );
    assertThat( eval( "class(x)" ), elementsIdenticalTo( c( "numeric" )));
    assertThat( eval( "oldClass(x) "), identicalTo( NULL ));
    assertThat(eval("inherits(x, \"a\") "), elementsIdenticalTo(c(false)));
  }


  @Test
  public void declaredClasses() {

    eval( "x<-1 ");
    eval( "class(x) <- c(\"a\", \"b\") ");
    assertThat( eval( "inherits(x, \"a\")"), elementsIdenticalTo( c(true) ));
    assertThat( eval( "inherits(x, \"a\", TRUE)"), elementsIdenticalTo( c_i(1) ));
    assertThat( eval( "inherits(x, c(\"a\", \"b\", \"c\"), TRUE) "), elementsIdenticalTo( c_i(1, 2, 0) ));

  }

  @Test
  public void nextMethodWithExtraArguments() {

    eval("f.default <- function(x, drop = TRUE) drop ");
    eval("f.foo <- function(x) NextMethod('f', drop = FALSE)");
    eval("f <- function(x) UseMethod('f') ");
    
    eval("x <- 42");
    eval("class(x) <- c('foo') ");

    assertThat(eval("f(x)"), elementsIdenticalTo(c(false)));
  }

  @Test
  public void nextMethodWithExtraDuplicateArguments() {

    eval("f.default <- function(x, drop = TRUE) drop ");
    eval("f.foo <- function(x, drop) NextMethod('f', drop = FALSE)");
    eval("f <- function(x, drop) UseMethod('f') ");

    eval("x <- 42");
    eval("class(x) <- c('foo') ");

    assertThat(eval("f(x, drop = TRUE)"), elementsIdenticalTo(c(false)));
  }

}
