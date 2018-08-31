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

package org.renjin.compiler;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.parser.RParser;
import org.renjin.sexp.SEXP;

import java.io.StringReader;

import static org.hamcrest.MatcherAssert.assertThat;

public class SexpCompilerTest extends EvalTestCase {

  @Test
  public void environmentUpdated() throws Exception {

    eval("x <- 99");
    eval("y <- 44");

    compileAndEvaluate("if(x > 42) y <- 88");

    assertThat(eval("y"), elementsIdenticalTo(88));
  }

  @Test
  public void environmentConditionallyUpdated() throws Exception {

    eval("x <- 99");
    SEXP result = compileAndEvaluate("if(x > 42) y <- 88 * x");

    assertThat(result, elementsIdenticalTo(88d * 99d));
    assertThat(eval("y"), elementsIdenticalTo(88d * 99d));
  }

  private SEXP compileAndEvaluate(String source) throws Exception {
    return SexpCompiler.compileSexp(topLevelContext, topLevelContext.getGlobalEnvironment(),
        RParser.parseAllSource(new StringReader(source)))
        .getCompiledBody().evaluate(topLevelContext, topLevelContext.getGlobalEnvironment());
  }

}