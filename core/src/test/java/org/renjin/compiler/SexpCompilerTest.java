/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.compiler.builtins.ArgumentBounds;
import org.renjin.compiler.cfg.InlinedFunction;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.parser.RParser;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
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

  @Test
  public void switchByName() throws Exception {
    eval("x <- 'foo'");
    assertThat(compileAndEvaluate("switch(x, bar=91, foo=92)"), elementsIdenticalTo(92));
    assertThat(compileAndEvaluate("switch(x, bar=91, fooz=92)"), equalTo(Null.INSTANCE));
    assertThat(compileAndEvaluate("switch(x, bar=91, fooz=92, 93)"), elementsIdenticalTo(93));
  }

  @Test
  public void switchByPosition() throws Exception {
    eval("x <- 2");
    assertThat(compileAndEvaluate("switch(x, bar=91, foo=92)"), elementsIdenticalTo(92));
    assertThat(compileAndEvaluate("switch(1.5, 91, 92)"), elementsIdenticalTo(91));
    assertThat(compileAndEvaluate("switch(2.5, 91, 92)"), elementsIdenticalTo(92));
    assertThat(compileAndEvaluate("switch(1.7, 91, 92)"), elementsIdenticalTo(91));
  }

  @Test
  public void switchByUnknownType() throws Exception {
    eval("x <- 104");

    SEXP result;
    CompiledBody body = compile(Joiner.on("\n").join(
        "if(x > 50) {",
            "y <- 2",
            "} else {",
            "y <- 'foo'",
            "}",
            "switch(y, foo=41, bar=42)"));

    result = body.evaluate(topLevelContext, topLevelContext.getGlobalEnvironment());
    assertThat(result, elementsIdenticalTo(42));

    eval("x <- 30");

    result = body.evaluate(topLevelContext, topLevelContext.getGlobalEnvironment());
    assertThat(result, elementsIdenticalTo(41));
  }


  @Test
  public void switchStatement() throws Exception {
    eval("x <- 2");
    compileAndEvaluate("switch(x, NULL, { y <- 99 }); NULL");

    assertThat(eval("y"), elementsIdenticalTo(99));
  }

  @Test
  public void ncol() throws Exception {
    eval("m <- matrix(1:12, nrow=3)");
    assertThat(compileAndEvaluate("dim(m)[2L]"), elementsIdenticalTo(c_i(4)));
  }

  @Test
  public void ifelse() throws Exception {
    eval("t <- c(TRUE,FALSE)");
    eval("x <- c(91, 92)");
    eval("y <- c(41, 42)");
    assertThat(compileAndEvaluate("zz <- ifelse(t, x, y); zz"), elementsIdenticalTo(91, 42));
  }

  @Test
  public void storageMode() throws Exception {
    eval("x <- 99");
    assertThat(compileAndEvaluate("storage.mode(x)"), elementsIdenticalTo(c("double")));
  }

  @Test
  public void setStorageMode() throws Exception {
    eval("x <- 1:12");
    compileAndEvaluate("storage.mode(x) <- 'integer'");
  }

  @Test
  public void logicalComparison() throws IOException {
    eval("p <- 1:12");
    eval("q <- 100:300");

    RuntimeState runtimeState = new RuntimeState(topLevelContext, topLevelContext.getGlobalEnvironment());
    IRBodyBuilder bodyBuilder = new IRBodyBuilder(runtimeState);
    IRBody body = bodyBuilder.build(RParser.parseAllSource(new StringReader("p == 0 | q == 0")));

    SexpCompiler compiler = new SexpCompiler(runtimeState, body, false);
    compiler.types.execute();
    compiler.types.dumpBounds();
  }

  @Test
  public void kld() {
    eval("kld = function(p, q) sum(ifelse(p == 0 | q == 0, 0, log(p / q) * p))");

    RuntimeState parentState = new RuntimeState(topLevelContext, topLevelContext.getGlobalEnvironment());
    String[] params = new String[] { "p", "q" };
    SEXP closure = eval("kld");

    ValueBounds p = ValueBounds.builder()
        .setTypeSet(TypeSet.DOUBLE)
        .build();

    ValueBounds q = ValueBounds.builder()
        .setTypeSet(TypeSet.DOUBLE)
        .build();

    InlinedFunction inlinedFunction = new InlinedFunction("f", parentState, (Closure) closure, params);
    ValueBounds functionBounds = inlinedFunction.updateBounds(Arrays.asList(new ArgumentBounds(p), new ArgumentBounds(q)));

    System.out.println(functionBounds);
  }

  @Test
  public void variadicFunctions() throws Exception {
    eval("f <- function(...) length(list(...))");
    assertThat(compileAndEvaluate("f(1,2,3)"), elementsIdenticalTo(c_i(3)));
  }

  @Test
  public void dataFrameSingleSubset() throws Exception {
    eval("df <- data.frame(aa=1:3, b=4:6)");

    assertThat(compileAndEvaluate("df[['aa']]"), elementsIdenticalTo(c_i(1, 2, 3)));

  }

  private SEXP compileAndEvaluate(String source) throws Exception {
    return compile(source).evaluate(topLevelContext, topLevelContext.getGlobalEnvironment());
  }

  private CompiledBody compile(String source) throws InstantiationException, IllegalAccessException, IOException {
    return SexpCompiler.compileSexp(topLevelContext, topLevelContext.getGlobalEnvironment(),
        RParser.parseAllSource(new StringReader(source)))
        .getCompiledBody();
  }
}