package r.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.junit.Test;

import r.scripting.RenjinScriptEngineFactory;

public class RenjinScriptEngineTest {

  @Test
  public void helloWorld() throws ScriptException {
    // create a script engine manager
    ScriptEngineFactory factory = new RenjinScriptEngineFactory();
    // create a JavaScript engine
    ScriptEngine engine = factory.getScriptEngine();
    // evaluate JavaScript code from String
    engine.eval("print('Hello, World')");
  }
  
}
