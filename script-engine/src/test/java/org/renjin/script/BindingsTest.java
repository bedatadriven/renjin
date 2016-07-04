package org.renjin.script;

import org.junit.Assert;
import org.junit.Test;

import javax.script.*;

public class BindingsTest {


  @Test
  public void testJavaScriptScriptEngine() throws ScriptException {
    testScriptEngine("JavaScript", "print(msg);");
  }


  @Test
  public void testRenjinScriptScriptEngine() throws ScriptException {
    testScriptEngine("Renjin", "cat(msg)");
  }

  private void testScriptEngine(String scriptEngineName, String outputScript) throws ScriptException {

    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(scriptEngineName);
    if (scriptEngine == null) {
      throw new IllegalStateException("Could not load ScriptEngine: " + scriptEngineName);
    }

    Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);

    bindings.put("msg", "Hello World");

    scriptEngine.eval(outputScript);

    bindings.remove("msg");

    try {

      scriptEngine.eval(outputScript);
      Assert.fail("Should have thrown an exception");
    } catch (Exception se) {
      // expected exception: variable is not defined anymore
    }

  }
}
