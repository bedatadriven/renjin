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
    assertThat( eval( "class(x)" ), equalTo( c( "numeric" )));
    assertThat( eval( "oldClass(x) "), equalTo( NULL ));
    assertThat(eval("inherits(x, \"a\") "), equalTo(c(false)));
  }


  @Test
  public void declaredClasses() {

    eval( "x<-1 ");
    eval( "class(x) <- c(\"a\", \"b\") ");
    assertThat( eval( "inherits(x, \"a\")"), equalTo( c(true) ));
    assertThat( eval( "inherits(x, \"a\", TRUE)"), equalTo( c_i(1) ));
    assertThat( eval( "inherits(x, c(\"a\", \"b\", \"c\"), TRUE) "), equalTo( c_i(1, 2, 0) ));

  }

  @Test
  public void updateArguments() {
    SEXP x = new IntArrayVector(1, 2, 3);
    SEXP i = IntArrayVector.valueOf(1);
    PairList actuals = PairList.Node.fromArray(x, i);
    PairList formals = new PairList.Builder()
        .add(Symbol.get("x"), Null.INSTANCE)
        .add(Symbol.get("i"), Null.INSTANCE)
        .add(Symbol.get("j"), Null.INSTANCE)
        .add(Symbol.get("drop"), Null.INSTANCE)
        .build();

    Environment env = Environment.createGlobalEnvironment(Environment.createBaseEnvironment());
    SEXP ni = new IntArrayVector(3);
    env.setVariable(topLevelContext, "i", ni);
    env.setVariable(topLevelContext, "x", x);

    PairList updated = S3.updateArguments(actuals, formals, env, new ListVector());

    assertThat(updated.length(), equalTo(2));
    assertThat(updated.getElementAsSEXP(0), equalTo(x));
    assertThat(updated.getElementAsSEXP(1), equalTo(ni));


    System.out.println(updated);
  }
  
  @Test
  public void nextMethodWithExtraArguments() {

    eval("f.default <- function(x, drop = TRUE) drop ");
    eval("f.foo <- function(x) NextMethod('f', drop = FALSE)");
    eval("f <- function(x) UseMethod('f') ");
    
    eval("x <- 42");
    eval("class(x) <- c('foo') ");

    assertThat(eval("f(x)"), equalTo(c(false)));
  }

  @Test
  public void nextMethodWithExtraDuplicateArguments() {

    eval("f.default <- function(x, drop = TRUE) drop ");
    eval("f.foo <- function(x, drop) NextMethod('f', drop = FALSE)");
    eval("f <- function(x, drop) UseMethod('f') ");

    eval("x <- 42");
    eval("class(x) <- c('foo') ");

    assertThat(eval("f(x, drop = TRUE)"), equalTo(c(false)));
  }

}
