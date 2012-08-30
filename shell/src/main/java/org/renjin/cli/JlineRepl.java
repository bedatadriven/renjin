package org.renjin.cli;

import jline.console.ConsoleReader;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.parser.RParser;
import org.renjin.primitives.Warning;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * A Read-Eval-Print Loop that uses Jline for 
 * reading lines
 */
public class JlineRepl {
  
  public static void main(String[] args) throws Exception {
      new JlineRepl();
    
  }

  private Context topLevelContext;
  private ConsoleReader reader;
  private PrintWriter out;
  
  private RParser parser;
  
  public JlineRepl() throws Exception {
    
    reader = new ConsoleReader();
    out = new PrintWriter(reader.getOutput());
    
    this.topLevelContext = new StandaloneContextFactory().create();
    this.topLevelContext.getGlobals().getConnectionTable().getStdout().setOutputStream(out);
    this.topLevelContext.getGlobals().setSessionController(new JlineSessionController(reader));
    this.topLevelContext.init();
    
    parser = new RParser(new JlineReader(reader));
    try {
      loop();
    } finally {
      reader.getTerminal().restore();
    }
  }

  private void loop() throws IOException {

    do {
      reader.setPrompt("> ");
      if(!parser.parse()) {
        System.err.println("result = " + parser.getResult() + ", status = " + parser.getResultStatus());
      }

      SEXP exp = parser.getResult();
      if(exp == null) {
        continue;
      }

      // clean up last warnings from any previous run
      clearWarnings();

      try {
        SEXP result = topLevelContext.evaluate(exp, topLevelContext.getGlobalEnvironment());

        if(!topLevelContext.getGlobals().isInvisible()) {
          topLevelContext.evaluate(FunctionCall.newCall(Symbol.get("print"), result));
        }

        printWarnings();
      } catch(EvalException e) {
        reader.getOutput().append(e.getMessage());
        reader.getOutput().append("\n");
      } catch(Exception e) {
        e.printStackTrace(new PrintWriter(reader.getOutput()));
      }

    } while(true);
  }


  private void printWarnings() {
    SEXP warnings = topLevelContext.getEnvironment().getBaseEnvironment().getVariable(Warning.LAST_WARNING);
    if(warnings != Symbol.UNBOUND_VALUE) {
      topLevelContext.evaluate( FunctionCall.newCall(Symbol.get("print.warnings"), warnings),
        topLevelContext.getEnvironment().getBaseEnvironment());
    }
  }

  private void clearWarnings() {
    topLevelContext.getEnvironment().getBaseEnvironment().remove(Warning.LAST_WARNING);
  }
}
