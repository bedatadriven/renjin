package org.renjin.script;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Before;
import org.junit.Test;
import org.renjin.sexp.DoubleVector;


public class RenjinScriptEngineTest {

  private ScriptEngine engine;
  private Invocable invocableEngine;

  @Before
  public void setUp() {
    // create a script engine manager
    ScriptEngineManager factory = new ScriptEngineManager();
  
    engine = factory.getEngineByName("Renjin");   
    invocableEngine = (Invocable)engine;
  }
  
  @Test
  public void helloWorld() throws ScriptException {
    
    // evaluate R code from String
    engine.eval("print('Hello, World')");
  }
  
  @Test
  public void invokeFunction() throws ScriptException, NoSuchMethodException {
    engine.eval("f <- function(x) sqrt(x)");
    DoubleVector result = (DoubleVector)invocableEngine.invokeFunction("f", 4);
    
    assertThat(result.length(), equalTo(1));
    assertThat(result.get(0), equalTo(2d));
  }
  
  @Test
  public void invokeMethod() throws ScriptException, NoSuchMethodException {
    Object obj = engine.eval("list(execute=sqrt)");
    DoubleVector result = (DoubleVector)invocableEngine.invokeMethod(obj, "execute", 16);
    
    assertThat(result.length(), equalTo(1));
    assertThat(result.get(0), equalTo(4d));
  }
  
  public interface Calculator {
    double calculate(double x);
  }
  
  @Test
  public void getInterface() throws ScriptException {
    
    engine.eval("calculate <- function(x) sqrt(x)");
    Calculator calculator = invocableEngine.getInterface(Calculator.class);
    
    assertThat(calculator.calculate(64), equalTo(8d));
  }
  
}
