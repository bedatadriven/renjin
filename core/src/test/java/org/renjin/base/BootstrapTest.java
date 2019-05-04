/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.base;

import org.junit.Before;
import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class BootstrapTest {


  private Session session;

  @Before
  public void setup() {
    session = new SessionBuilder().withoutBasePackage().build();
  }

  @Test
  public void internalCall() {
    eval(" .Internal(intToBits(1L))");
  }

  @Test
  public void list() {
    eval(" f<- function(...) list(...) ");
    eval("f(1,2,3)");
  }

  @Test
  public void methodCall() {


    eval(" f.foo <- function(x, y, z) c(x, y, z) ");
    eval(" f <- function(x, y, z) UseMethod('f') ");

    session.getGlobalEnvironment().setVariableUnsafe("foo",
        new IntArrayVector(
            new int[] { 1},
            AttributeMap.newBuilder().setClass("foo").build()));

    eval(" f(foo, 3, 4) ");
  }

  @Test
  public void listWithNull() {
    ListVector list = (ListVector) eval("list(NULL)");
    assertThat(list.length(), equalTo(1));
  }

  @Test
  public void asFunction() {
    eval("as.function <- function (x) .Internal(as.function.default(x, baseenv()))");
    eval("as.list <- function(x) .Internal(as.vector(x, 'list'))");
    eval("formals <- function(x) .Internal(formals(fun))");
    eval("fun <- function(x) NULL");
    eval("value <- 42");
    eval("fun <- as.function(c(as.list(formals(fun)), list(value)))");
    System.out.println(eval("fun()"));
  }

  private SEXP eval(String s) {
    return session.getTopLevelContext().evaluate(RParser.parseSource(s));
  }
}
