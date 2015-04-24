package org.renjin.primitives;


import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;

public class FinalizersTest {

  @Test
  public void test() {

    Session session = SessionBuilder.buildDefault();

    // I don't want to insert calls to run finalizers everywhere
    // so far now it's up to the Renjin caller to invoke runFinalizers()
    // periodically if needed.

    session.runFinalizers();
    session.getTopLevelContext().evaluate(RParser.parseSource(
        "reg.finalizer(.GlobalEnv, f=function(e) cat(environmentName(e)), onexit=TRUE);"));
    session.runFinalizers();
    session.getTopLevelContext().evaluate(RParser.parseSource("gc();"));

    // Finalizers with onexit = TRUE will run when the session is closed
    session.close();
  }

}