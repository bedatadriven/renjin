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

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.parser.RParser;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.sexp.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ClosureCompilerTest extends EvalTestCase {


  @Test
  public void simpleTest() throws IllegalAccessException {

    eval("g <- function(a, b) a * b");
    eval(
        "f <- function(x) {",
        "  if(x > 10) {",
        "    g(b=x,a=2)",
        "  } else {",
        "    sqrt(x)",
        "  }",
        "}");

    compileFunctions();

    SEXP result = eval("f(42)");

    assertThat(result, equalTo(DoubleVector.valueOf(84)));
  }

  @Test
  public void logicalOrTest() throws IllegalAccessException {

    eval("f <- function(a, b) a || b\n");

    compileFunctions();

    SEXP result = eval("f(logical(0), FALSE)");

    assertThat(result, equalTo(LogicalVector.NA_VECTOR));
  }

  @Test
  public void functionResolution() throws IllegalAccessException {

    eval("f <- function(x) { sqrt <- 16; sqrt(x) + sqrt; }");

    compileFunctions();

    assertThat(eval("f(64)"), identicalTo(new DoubleArrayVector(24)));
  }

  /**
   * There is no way to tell whether sqrt is a function or not, so we have to fall back
   * to dynamic lookup.
   */
  @Test
  public void functionResolutionBad() throws IllegalAccessException {

    eval("f <- function(x, y) { sqrt <- y; sqrt(x) + sqrt; }");

    compileFunctions();

    assertThat(eval("f(64, 1)"), identicalTo(new DoubleArrayVector(9)));
  }

  @Test
  public void compileBaseFunction() throws IllegalAccessException {
    compileBaseFunction("readBin");
  }

  private SEXP eval(String... lines) {

    ExpressionVector source = RParser.parseSource(Joiner.on("\n").join(lines) + "\n", "test.R");

    return topLevelContext.evaluate(source);
  }

  private void compileFunctions() throws IllegalAccessException {

    Environment global = topLevelContext.getGlobalEnvironment();
    for (Symbol symbolName : global.getSymbolNames()) {
      compileFunction(global, symbolName);
    }
  }

  private void compileBaseFunction(String name) throws IllegalAccessException {
    compileFunction(topLevelContext.getBaseEnvironment(), Symbol.get(name));
  }

  private void compileFunction(Environment env, Symbol symbolName) throws IllegalAccessException {
    SEXP value = env.getVariableUnsafe(symbolName).force(topLevelContext);
    if(value instanceof Closure) {

      Closure closure = (Closure) value;

      ClosureCompiler compiler = new ClosureCompiler(topLevelContext, closure);
      AotHandle handle = compiler.getHandle();

      closure.compiledBody = handle.loadAndGetHandle();
      closure.frameSymbols = handle.getLocalVars().toArrayUnsafe();
    }
  }

}