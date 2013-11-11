package org.renjin;

import org.junit.Before;
import org.junit.BeforeClass;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Deparse;
import org.renjin.primitives.Identical;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.SEXP;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public abstract class AbstractMicroTest {

  public static RenjinScriptEngine engine;

  @BeforeClass
  public static void setupScriptEngine() {
    engine = new RenjinScriptEngineFactory().getScriptEngine();
  }

  @Before
  public void cleanUpGlobalEnvironment() {
    engine.getSession().getGlobalEnvironment().clear();
  }

  protected final void assertIdentical(String x, String y) {
    SEXP xexp;
    try {
      xexp = (SEXP)engine.eval(x);
    } catch (ScriptException e) {
      throw new RuntimeException("Error evaluating: " + x, e);
    }

    SEXP yexp = null;
    try {
      yexp = (SEXP)engine.eval(y);
    } catch (ScriptException e) {
      throw new RuntimeException("Error evaluating: " + y, e);
    }

    if(!Identical.identical(xexp, yexp)) {
      throw new AssertionError("Expecting '" + y + "', got: '" +
        Deparse.deparse(engine.getTopLevelContext(), xexp, 80, false, 0, 0) + "'");
    }
  }

}
