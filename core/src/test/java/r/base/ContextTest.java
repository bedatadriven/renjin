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

import org.junit.Test;
import r.EvalTestCase;
import r.lang.exception.FunctionCallException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ContextTest extends EvalTestCase {

  @Test
  public void nframesGlobal() {
    assertThat( eval(".Internal(sys.nframe())"), equalTo( c_i(0)));
  }

  @Test
  public void nframesInClosure() {
    eval( "sys.nframe <- function() .Internal(sys.nframe()) ");
    eval( "f <- function() { sys.nframe() }");
    eval( "g <- function() f() ");
    eval( "h <- function() g() ");

    assertThat( eval(" h() "), equalTo( c_i(3) ));
  }

  @Test
  public void parentFrameInGlobal() {
    eval( "parent.frame <- function(n = 1) .Internal(parent.frame(n)) ");

    assertThat(eval("parent.frame()"), is(GlobalEnv));
    assertThat(eval("parent.frame(1)"), is(GlobalEnv));
    assertThat(eval("parent.frame(99)"), is(GlobalEnv));
  }

  @Test(expected = FunctionCallException.class)
  public void parentFrameInvalidArg() {
    eval(" .Internal(parent.frame(-1)) ");
  }

  @Test
  public void parentFrameClosure() {
    eval(" parent.frame <- function(n=1) .Internal(parent.frame(n)) ");
    eval(" g <- function() { parent.frame()$zz + 1 } ");
    eval(" f <- function() { zz<-41; g() } ");

    assertThat( eval("f()"), equalTo( c(42) ));
  }

  @Test
  public void parentFrameInFormals() {
    eval(" parent.frame <- function(n=1) .Internal(parent.frame(n)) ");
    eval(" g<- function(env = parent.frame()) env$zzz * 2");
    eval(" f<- function() { zzz<-21; g() } ");

    assertThat( eval("f()"), equalTo( c(42)));
  }
}
