package org.renjin.test;

import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class AlphaTest {
  
  @Test
  public void scriptEngineTest() throws ScriptException {
    ScriptEngineManager sem = new ScriptEngineManager();
    ScriptEngine renjin = sem.getEngineByName("Renjin");
    renjin.eval("library(org.renjin.test.alpha)");
    renjin.eval("stopifnot(parseAlpha('true')$nodeType == 'BOOLEAN')");
  }
}
