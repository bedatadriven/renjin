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

package org.renjin.compiler.aot;

import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.sexp.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ClosureCompilerTest {

  @Test
  public void simpleTest() throws InvocationTargetException, IllegalAccessException {

    Session session = new SessionBuilder().build();
    session.getTopLevelContext().evaluate(RParser.parseSource("g <- function(a, b) a * b\n"));

    ExpressionVector source = RParser.parseSource(Joiner.on('\n').join(
        "f <- function(x) {",
        "  if(x > 10) {",
        "    g(b=x,a=2)",
        "  } else {",
        "    sqrt(x)",
        "  }",
        "}\n"), "test.R");

    session.getTopLevelContext().evaluate(source);
    Closure closure = (Closure) session.getTopLevelContext().evaluate(Symbol.get("f"));

    ClosureCompiler compiler = new ClosureCompiler(session, closure);

    Method method = compiler.getHandle().loadAndReflect();

    SEXP[] args = {DoubleVector.valueOf(14)};
    SEXP result = (SEXP) method.invoke(null, session.getTopLevelContext(), session.getGlobalEnvironment(), args);

    assertThat(result, equalTo(DoubleVector.valueOf(28)));
  }

  @Test
  public void scaleTest() throws InvocationTargetException, IllegalAccessException {

    Session session = new SessionBuilder().build();
    Closure closure = (Closure) session.getTopLevelContext().evaluate(Symbol.get("scale.default"));

    ClosureCompiler compiler = new ClosureCompiler(session, closure);

    Method method = compiler.getHandle().loadAndReflect();

    DoubleVector x = new DoubleArrayVector(1, 2, 3);

    SEXP result = (SEXP) method.invoke(null, session.getTopLevelContext(), session.getGlobalEnvironment(), new SEXP[]{x, LogicalVector.TRUE, LogicalVector.FALSE});

    System.out.println(result);

  }

}