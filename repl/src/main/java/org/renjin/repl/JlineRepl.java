package org.renjin.repl;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import org.renjin.RenjinVersion;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.ParseOptions;
import org.renjin.parser.ParseState;
import org.renjin.parser.RLexer;
import org.renjin.parser.RParser;
import org.renjin.parser.RParser.StatusResult;
import org.renjin.primitives.Warning;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * A Read-Eval-Print Loop that uses Jline for 
 * reading lines
 */
public class JlineRepl {


  /**
   * Echo lines to standard out.
   */
  private boolean echo;

  /**
   * Whether to abort evaluation if an error is encountered
   */
  private boolean stopOnError;
  private JlineSessionController sessionController;

  public static void main(String[] args) throws Exception {
    JlineRepl repl = new JlineRepl(SessionBuilder.buildDefault());
    repl.run();
  }

  private final Session session;
  private final Context topLevelContext;
  private ConsoleReader reader;

  public JlineRepl(Session session) throws Exception {
    this.session = session;
    this.topLevelContext = session.getTopLevelContext();

    if(Strings.nullToEmpty(System.getProperty("os.name")).startsWith("Windows")) {
      // AnsiWindowsTerminal does not work properly in WIndows 7
      // so disabling across the board for now
      reader = new ConsoleReader(System.in, System.out, new UnsupportedTerminal());
    } else {
      reader = new ConsoleReader();
    }

    // disable events triggered by ! (this is valid R !!)
    reader.setExpandEvents(false);
    reader.setHandleUserInterrupt(true);
    session.setSessionController(new JlineSessionController(reader.getTerminal()));
  }

  public JlineRepl(Session session, ConsoleReader reader) throws IOException {
    this.session = session;
    sessionController = new JlineSessionController(reader.getTerminal());
    this.session.setSessionController(sessionController);
    this.topLevelContext = session.getTopLevelContext();
    this.reader = reader;
  }
  
  public void setInteractive(boolean interactive) {
    sessionController.setInteractive(interactive);
  }

  public boolean isInteractive() {
    return sessionController.isInteractive();
  }
  
  public void setEcho(boolean echo) {
    this.echo = echo;
  }

  public boolean isEcho() {
    return echo;
  }

  public boolean isStopOnError() {
    return stopOnError;
  }

  public void setStopOnError(boolean stopOnError) {
    this.stopOnError = stopOnError;
  }

  public void run() throws Exception {

    reader.getTerminal().init();

    printGreeting();

    try {
      while(readExpression()) { }

      // run finalizers and shutdown
      session.close();

    } finally {
      reader.getTerminal().restore();
    }
  }

  private void printGreeting() throws Exception {

    try {
      reader.println("Renjin " + RenjinVersion.getVersionName());
    } catch (IOException e) {
      reader.println("Renjin");
    }

    reader.println("Copyright (C) 2015 The R Foundation for Statistical Computing");
    reader.println("Copyright (C) 2015 BeDataDriven");

  }

  private boolean readExpression() throws Exception {

    reader.setPrompt("> ");

    ParseOptions options = new ParseOptions();
    ParseState parseState = new ParseState();
    JlineReader lineReader = new JlineReader(reader);
    lineReader.setEcho(echo);
    lineReader.setEchoOut(reader.getOutput());

    SEXP exp;
    try {
      RLexer lexer = new RLexer(options, parseState, lineReader);
      if (lexer.isEof()) {
        return false;
      }

      RParser parser = new RParser(options, parseState, lexer);
      while (!parser.parse()) {
        if (lexer.errorEncountered()) {
          String errorMessage = "Syntax error at " + lexer.getErrorLocation() + ": " + lexer.getErrorMessage();
          reader.getOutput().append(errorMessage + "\n");
          if (stopOnError) {
            throw new RuntimeException(errorMessage);
          }
        }
      }


      exp = parser.getResult();
      if(parser.getResultStatus() == StatusResult.EOF) {
        return true;
      } else if(exp == null) {
        return true;
      }
    } catch (UserInterruptException e) {
      reader.resetPromptLine("> ", "", 0);
      reader.println();
      return true;
    }

    // clean up last warnings from any previous run
    clearWarnings();

    try {
      SEXP result = topLevelContext.evaluate(exp, topLevelContext.getGlobalEnvironment());

      if(!session.isInvisible()) {
        topLevelContext.evaluate(FunctionCall.newCall(Symbol.get("print"), Promise.repromise(result)));
      }

      printWarnings();
    } catch(EvalException e) {
      printEvalException(e);
      if(stopOnError) {
        throw e;
      }
    } catch(QuitException e) {
      return false;
    } catch(Exception e) {
      printException(e);
      if(stopOnError) {
        throw e;
      }
    }
    return true;
  }


  private void printException(Exception e) throws IOException {
    reader.getOutput().append("ERROR: " + e.getMessage());
    PrintWriter printWriter = new PrintWriter(reader.getOutput());
    e.printStackTrace(printWriter);
    printWriter.flush();
    reader.getOutput().flush();
  }

  private void printEvalException(EvalException e) throws IOException {
    reader.getOutput().append("ERROR: ").append(e.getMessage()).append("\n");
    if (e.getCause() != null) {
      reader.getOutput().write(Throwables.getStackTraceAsString(e.getCause()));
    }
    e.printRStackTrace(reader.getOutput());
    reader.getOutput().flush();
  }

  private void printWarnings() {
    SEXP warnings = topLevelContext.getBaseEnvironment().getVariable(Warning.LAST_WARNING);
    if(warnings != Symbol.UNBOUND_VALUE) {
      topLevelContext.evaluate( FunctionCall.newCall(Symbol.get("print.warnings"), warnings),
              topLevelContext.getBaseEnvironment());
    }
  }

  private void clearWarnings() {
    topLevelContext.getBaseEnvironment().remove(Warning.LAST_WARNING);
  }

  public ConsoleReader getReader() {
    return reader;
  }
}
