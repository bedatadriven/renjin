package org.renjin.script;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.renjin.sexp.*;

import javax.script.*;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class RenjinScriptEngineTest {

  private ScriptEngine engine;
  private Invocable invocableEngine;

  @Before
  public void setUp() {
    // create a script engine manager
    ScriptEngineManager factory = new ScriptEngineManager();
  
//    engine = factory.getEngineByName("Renjin");   
//    if(engine == null) {
//      throw new AssertionError("Failed to create Renjin Script Engine");
//    }
    engine = new RenjinScriptEngineFactory().getScriptEngine();
    invocableEngine = (Invocable)engine;
  }
  
  @Test
  public void helloWorld() throws ScriptException {
    
    // evaluate R code from String
    engine.eval("print('Hello, World')");
  }

  @Test
  public void statsPackageIsLoadedByDefault() throws ScriptException {
    engine.eval("dnorm(0)");
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
  
  @Test
  public void putJavaObject() throws ScriptException {
    // create a script engine manager
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();

    // create an R engine
    ScriptEngine engine = factory.getScriptEngine();

    HashMap<String, String> h = new HashMap<String, String>();
    engine.put("h", h);
    engine.eval("import(java.util.HashMap) \n"
        + " print(h$size())");
  }

  @Test
  public void putPrimitiveIntegerArray() throws ScriptException {
    int[] integers = new int[]{1,2,3,4,5};
    engine.put("integers", integers);
    IntArrayVector iav =  (IntArrayVector) engine.eval("as.integer(integers)");
    System.out.println(iav);
    assertThat(iav.length(), equalTo(5));
  }

  @Test
  public void putPrimitiveLongArray() throws ScriptException {
    long[] longs = new long[]{1,2,3,4,5};
    engine.put("longs", longs);
    IntArrayVector lav =  (IntArrayVector) engine.eval("as.integer(longs)");
    assertThat(lav.length(), equalTo(5));
  }

  @Test
  public void putPrimitiveDoubleArray() throws ScriptException {
    double[] doubles = new double[]{1.0,2.0,3.0,4.0,5.0};
    engine.put("doubles", doubles);
    DoubleArrayVector dav =  (DoubleArrayVector) engine.eval("as.double(doubles)");
    assertThat(dav.length(), equalTo(5));
  }
  
  @Test
  public void putNullRef() throws ScriptException {
    
    engine.put("x", 42);
    assertThat(engine.eval("x == 42"), CoreMatchers.<Object>equalTo(LogicalVector.TRUE));

    engine.put("x", null);
    assertThat(engine.eval("is.null(x)"), CoreMatchers.<Object>equalTo(LogicalVector.TRUE));
  }
  
  @Test
  public void putNullRefOnBindings() throws ScriptException {
    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
    
    bindings.put("x", null);
    assertThat(engine.eval("is.null(x)"), CoreMatchers.<Object>equalTo(LogicalVector.TRUE));
  }
  
  @Test
  public void readResource() throws ScriptException {
    StringVector vector = (StringVector) engine.eval("readLines('res:org/renjin/test.txt')");
    assertThat(vector.length(), equalTo(1));
    assertThat(vector.getElementAsString(0), equalTo("hello world"));
  }


  @Test
  public void sameBindingsFromEngineAndContext() throws ScriptException {

    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("Renjin");
    if (scriptEngine == null) {
      throw new IllegalStateException("Could not load ScriptEngine: Renjin");
    }

    Bindings bindingsFromEngine = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);

    Bindings bindingsFromContext = scriptEngine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);

    assertTrue(bindingsFromEngine == bindingsFromContext); // identity test

    final String key = "foo";
    final String value = "bar";

    Assert.assertNull(bindingsFromEngine.get(key));
    Assert.assertNull(bindingsFromContext.get(key));

    bindingsFromEngine.put(key, value);
    Assert.assertEquals(bindingsFromEngine.get(key), value);

    Assert.assertEquals(bindingsFromContext.get(key), value);
  }
}
