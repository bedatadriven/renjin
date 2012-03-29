package org.renjin.compiler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.compiler.ClosureCompiler;
import org.renjin.parser.RParser;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


public class ClosureCompilerTest extends EvalTestCase {

  @Test
  public void simplestTest() throws Exception {
    eval("f <- function(a,b) a+b");
    
    compileClosure("f", "fc");
    assertThat(eval("fc(2,6)"), equalTo(c(8)));
  }
  
  @Test
  public void primitiveElipses() throws Exception {
    eval("x <- c(3,4,5) ");
    eval("f <- function(...) x[...] ");
    compileClosure("f", "fc");
    assertThat(eval("fc(1)"), equalTo(c(3)));
  }
  
  @Test
  public void dynamicElipses() throws Exception {
    eval("x <- c(3,4,5) ");
    eval("f <- function(...) list(...) ");
    compileClosure("f", "fc");
    assertThat(eval("fc(1,2,3,4)"), equalTo(list(1d,2d,3d,4d)));
  }

  @Test
  public void colMeans() throws Exception {
    source("colMeans.R");
    compileClosure("colMeans", "colMeans.compiled");
  }
  
  @Test
  public void message() throws Exception {
    source("message.R");
    compileClosure("message", "message.compiled");
  }
  
  @Test
  public void Map() throws Exception  {
    eval(" Map <- function(f, ...) mapply(f, ..., SIMPLIFY = FALSE)");
    compileClosure("Map", "Map.compiled");
  }

  private void source(String string) throws IOException {
    ExpressionVector source = RParser.parseSource(new InputStreamReader(getClass().getResourceAsStream(string)));
    topLevelContext.evaluate(source);
  }
  
  private void compileClosure(String src, String dest) throws InstantiationException,
      IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    SEXP closureSexp = topLevelContext
            .getEnvironment().getVariable(src);
    Class<Closure> closureClass = ClosureCompiler.compileAndLoad((Closure)closureSexp);
    
    Closure compiled = closureClass.getConstructor(Environment.class).newInstance(topLevelContext.getEnvironment());
    
    topLevelContext.getEnvironment().setVariable(Symbol.get(dest), compiled);
  }
  
}
