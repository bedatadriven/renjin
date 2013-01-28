package org.renjin.repl;

import java.io.IOException;
import java.io.PrintWriter;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.parser.RParser.StatusResult;
import org.renjin.primitives.Warning;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import com.google.common.base.Strings;

/**
 * A Read-Eval-Print Loop that uses Jline for 
 * reading lines
 */
public class JlineRepl {
  
  public static void main(String[] args) throws Exception {
    new JlineRepl(SessionBuilder.buildDefault());    
  }

  private Context topLevelContext;
  private ConsoleReader reader;
  private PrintWriter out;
  
  private RParser parser;
  private JlineReader jlineReader;
  
  public JlineRepl(Session session) throws Exception {
    
    if(Strings.nullToEmpty(System.getProperty("os.name")).startsWith("Windows")) {
      // AnsiWindowsTerminal does not work properly in WIndows 7
      // so disabling across the board for now
      reader = new ConsoleReader(System.in, System.out, new UnsupportedTerminal());
    } else {
      reader = new ConsoleReader();
    }
    
    reader.getTerminal().init();

    out = new PrintWriter(reader.getOutput());
    
    this.topLevelContext = session.getTopLevelContext();
   
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
        if(jlineReader.isEof()) {
          return;
        }
        System.err.println("result = " + parser.getResult() + ", status = " + parser.getResultStatus());
      }

      SEXP exp = parser.getResult();
      if(parser.getResultStatus() == StatusResult.EOF) {
        return;
      } else if(exp == null) {
        continue;
      }

      // clean up last warnings from any previous run
      clearWarnings();

      try {
        SEXP result = topLevelContext.evaluate(exp, topLevelContext.getGlobalEnvironment());

        if(!topLevelContext.getSession().isInvisible()) {
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
