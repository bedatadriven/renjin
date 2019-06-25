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
package org.renjin.compiler.aot;

import org.junit.Before;
import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.sexp.*;

import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ClosureCompilerTest {

  private Session session;

  @Before
  public void setUp() {
    session = new SessionBuilder().withoutBasePackage().build();
  }

  @Test
  public void simpleTest() throws InvocationTargetException, IllegalAccessException {

    eval("g <- function(a, b) a * b");

    compileFunction("f <- function(x) {",
        "  if(x > 10) {",
        "    g(b=x,a=2)",
        "  } else {",
        "    sqrt(x)",
        "  }",
        "}\n");

    assertThat(eval("f(42)"), equalTo(DoubleVector.valueOf(84)));
  }

  @Test
  public void lengthTest() throws IllegalAccessException {

    eval("g <- function(a, b) a * b");

    compileFunction("f <- function(...) {",
        "    g(...)",
        "}\n");

    assertThat(eval("f(2, 42)"), equalTo(DoubleVector.valueOf(84)));

  }

  private SEXP eval(final String source) {
    return session.getTopLevelContext().evaluate(RParser.parseSource(source + "\n"));
  }

  private void compileFunction(String... lines) throws IllegalAccessException {

    ExpressionVector source = RParser.parseSource(Joiner.on('\n').join(lines), "test.R");

    session.getTopLevelContext().evaluate(source);
    Closure closure = (Closure) session.getTopLevelContext().evaluate(Symbol.get("f"));

    ClosureCompiler compiler = new ClosureCompiler(session.getTopLevelContext(), closure);

    closure.compiledBody = compiler.getHandle().loadAndGetHandle();

  }

  @Test
  public void promises() throws IllegalAccessException {
    Session session = new SessionBuilder().build();
    session.getTopLevelContext().evaluate(RParser.parseSource(
        "g <- function(a) substitute(a)\n" +
        "f <- function() g(stop(x + y))\n", "test.R"));

    Closure closure = (Closure) session.getTopLevelContext().evaluate(Symbol.get("f"));
    ClosureCompiler compiler = new ClosureCompiler(session.getTopLevelContext(), closure);
    closure.compiledBody = compiler.getHandle().loadAndGetHandle();

    FunctionCall result = (FunctionCall) session.getTopLevelContext().evaluate(FunctionCall.newCall(Symbol.get("f")));
  }


}