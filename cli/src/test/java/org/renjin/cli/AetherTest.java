package org.renjin.cli;

import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;

public class AetherTest {

  @Test
  public void resolve() throws Exception {
    Session session = Main.createSession();
    session.getTopLevelContext().evaluate(FunctionCall.newCall(Symbol.get("library"), Symbol.get("survival")));
    
  }
  
}
