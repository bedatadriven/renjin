package org.renjin;

import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;

import javax.script.ScriptException;
import java.util.concurrent.Executors;

public class FortranCallTest {
  
  @Test
  public void test() throws ScriptException {
    Session session = new SessionBuilder()
        .setExecutorService(Executors.newFixedThreadPool(4))
        .build();

    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine scriptEngine = factory.getScriptEngine(session);

    scriptEngine.eval("library(stats)");
    scriptEngine.eval("x <- 1:1000 + 2");
    scriptEngine.eval("k <- kmeans(x, 3)");
    scriptEngine.eval("print(k$withinss)");
  }
}
