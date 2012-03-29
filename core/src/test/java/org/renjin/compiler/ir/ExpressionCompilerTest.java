package org.renjin.compiler.ir;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.compiler.CompiledBody;
import org.renjin.compiler.ExpressionCompiler;
import org.renjin.compiler.ThunkMap;
import org.renjin.compiler.runtime.VariablePromise;
import org.renjin.parser.RParser;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Vector;


public class ExpressionCompilerTest extends EvalTestCase {
  

  @Test
  public void simplestTest() throws Exception {
    topLevelContext.getEnvironment().setVariable(Symbol.get("x"), new DoubleVector(1,2,3,4));
    DoubleVector result = (DoubleVector) compileAndEval("x<-4; x\n");
    assertThat(result.getElementAsDouble(0), equalTo(4d));
  }
  
  public void variableThunk() {
    Promise exp = new VariablePromise(topLevelContext, "foo");
    exp.force();
  }
  
  @Test
  public void ifStatement() throws Exception {
    DoubleVector result = (DoubleVector) compileAndEval("if(TRUE) 42 else 5\n");
    assertThat(result.getElementAsDouble(0), equalTo(42d));
  }
    
  @Test
  public void dynamicCall() throws Exception {
    SEXP result = compileAndEval("x<-5; length(x)\n");
    assertThat(((Vector)result).getElementAsInt(0), equalTo(1));
  }
  
  @Test
  public void dynamicCallToClosure() throws Exception {
    topLevelContext.evaluate(RParser.parseSource("f<-function(x) sqrt(x);"));
    assertThat( compileAndEval("y<-16; f(y)\n"), equalTo(c(4)));
    assertThat( compileAndEval("f(16)\n"), equalTo(c(4)));
    //assertThat( compileAndEval("f(1+1)\n"), equalTo(c(4)));
  }
  
  @Test
  public void primitiveCall() throws Exception {
    DoubleVector result = (DoubleVector) compileAndEval("1 + 1\n");
    assertThat(result.getElementAsInt(0), equalTo(2));
  }
  
  @Test
  public void forLoop() throws Exception {
    DoubleVector result = (DoubleVector) compileAndEval("x <- 0; for(i in 1:10) { x <- x + 10 }; x \n");
    assertThat(result.getElementAsInt(0), equalTo(100));
  }

  @Test
  public void envVars() throws Exception {
	  DoubleVector result = (DoubleVector) compileAndEval("a <- 1; b <- 2; c <- a + b; c \n");
	  assertThat(result.getElementAsInt(0), equalTo(3));
  }

  private SEXP compileAndEval(String code)
      throws InstantiationException, IllegalAccessException {
    ExpressionVector exp = RParser.parseSource(code);
    ThunkMap thunkMap = new ThunkMap("r/anon/Thunk");
    Class<CompiledBody> compiled = ExpressionCompiler.compile(thunkMap, exp);
    
    return compiled.newInstance().eval(topLevelContext, topLevelContext.getEnvironment());
  }
  
  
}
