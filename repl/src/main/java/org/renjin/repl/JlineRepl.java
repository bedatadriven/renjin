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
package org.renjin.repl;

import com.github.fommil.netlib.BLAS;
import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import org.renjin.RenjinVersion;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.*;
import org.renjin.parser.RParser.StatusResult;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A Read-Eval-Print Loop that uses Jline for 
 * reading lines
 */
public class JlineRepl {

  private final Session session;
  private final Context topLevelContext;
  private final ConsoleReader reader;
  private final JlineSessionController sessionController;

  /**
   * Echo lines to standard out.
   */
  private boolean echo;

  private PrintStream errorStream;
  
  /**
   * Whether to abort evaluation if an error is encountered
   */
  private boolean stopOnError;


  public JlineRepl(Session session, ConsoleReader reader) throws IOException {
    this.session = session;
    this.topLevelContext = session.getTopLevelContext();
    this.reader = reader;
    this.sessionController = new JlineSessionController(reader);
    this.session.setSessionController(sessionController);
    this.errorStream = new PrintStream(System.err);
  }

  public JlineRepl(Session session) throws Exception {
    this(session, createInteractiveConsoleReader());
  }

  private static ConsoleReader createInteractiveConsoleReader() throws IOException {
    ConsoleReader reader;
    if(Strings.nullToEmpty(System.getProperty("os.name")).startsWith("Windows")) {
      // AnsiWindowsTerminal does not work properly in WIndows 7
      // so disabling across the board for now
      reader = new ConsoleReader(System.in, System.out, new UnsupportedTerminal());
    } else {
      reader = new ConsoleReader();
    }
    reader.setExpandEvents(false); // disable events triggered by "!", which is a valid R token
    reader.setHandleUserInterrupt(true);
    return reader;
  }

  public PrintStream getErrorStream() {
    return errorStream;
  }

  public void setErrorStream(PrintStream errorStream) {
    this.errorStream = errorStream;
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

    reader.println("Copyright (C) 2018 The R Foundation for Statistical Computing");
    reader.println("Copyright (C) 2018 BeDataDriven");

    printBlasLibrary();

  }

  private void printBlasLibrary() throws IOException {
    String impl = BLAS.getInstance().getClass().getSimpleName();
    switch (impl) {
      case "NativeRefBLAS":
        reader.println("Using native reference BLAS libraries.");
        break;
      case "NativeSystemBLAS":
        reader.println("Using system BLAS libraries.");
        break;
      default:
        reader.println("Falling back to pure JVM BLAS libraries.");
        break;
    }
  }

  private boolean readExpression() throws Exception {

    reader.setPrompt("> ");

    ParseOptions options = new ParseOptions();
    ParseState parseState = new ParseState();
    JlineReader lineReader = new JlineReader(reader);
    lineReader.setEcho(echo);
    lineReader.setEchoOut(reader.getOutput());

    List<SEXP> exprList = new ArrayList<>();
    try {
      RLexer lexer = new RLexer(options, parseState, lineReader);
      if (lexer.isEof()) {
        return false;
      }

      RParser parser = new RParser(options, parseState, lexer);

      parseLoop: while (true) {

        // check to see if we are at the end of the file
        if(lexer.isEof()) {
          return false;
        }

        if (!parser.parse()) {
          if (lexer.errorEncountered()) {
            reader.getOutput().flush();
            String errorMessage = "Syntax error at " + lexer.getErrorLocation() + ": " + lexer.getErrorMessage();
            errorStream.println(errorMessage);
            errorStream.flush();
            reader.killLine();
            if (stopOnError) {
              throw new RuntimeException(errorMessage);
            }
            break parseLoop;
          }
        }

        StatusResult status = parser.getResultStatus();
        switch (status) {
          case EMPTY:
            break;

          case INCOMPLETE:
          case OK:
            exprList.add(parser.getResult());
            break;

          case ERROR:
            throw new ParseException(parser.getResultStatus().toString());

          case EOF:
            break parseLoop;
        }
        if(lineReader.isEndOfLine()) {
          break;
        }
      }

      if(exprList.isEmpty()) {
        return true;
      }
    } catch (UserInterruptException e) {
      reader.resetPromptLine("> ", "", 0);
      reader.println();
      return true;
    }

    // clean up last warnings from any previous run
    session.clearWarnings();

    try {
      SEXP result = topLevelContext.evaluate(new ExpressionVector(exprList), topLevelContext.getGlobalEnvironment());

      if(!session.isInvisible()) {
        topLevelContext.evaluate(FunctionCall.newCall(Symbol.get("print"), Promise.repromise(result)));
      }

      session.printWarnings();
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
    reader.getOutput().flush();
    errorStream.println("ERROR: " + e.getMessage());
    e.printStackTrace(errorStream);
    errorStream.flush();
  }

  private void printEvalException(EvalException e) throws IOException {
    reader.getOutput().flush();
    errorStream.println("ERROR: " + e.getMessage());
    if (e.getCause() != null) {
      e.getCause().printStackTrace(errorStream);
    }
    e.printRStackTrace(errorStream);
    errorStream.flush();
  }


  public ConsoleReader getReader() {
    return reader;
  }


  public void close() {
    maybeShutdownGraphicsDevices();
  }

  /**
   * If the grDevices namespace is loaded, then close up any open graphics devices.
   */
  private void maybeShutdownGraphicsDevices() {
    session.getNamespaceRegistry().getNamespaceIfPresent(Symbol.get("grDevices")).ifPresent(namespace -> {
      SEXP shutdownFunction = namespace.getEntry(Symbol.get("shutdown"));
      session.getTopLevelContext().evaluate(FunctionCall.newCall(shutdownFunction));
    });
  }

  public static void main(String[] args) throws Exception {
    JlineRepl repl = new JlineRepl(SessionBuilder.buildDefault());
    repl.run();
  }

}
