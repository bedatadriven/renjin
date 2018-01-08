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
