/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.studio.console;

import org.apache.commons.vfs2.FileSystemException;
import org.renjin.RenjinVersion;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.parser.*;
import org.renjin.primitives.Warning;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;
import org.renjin.studio.StudioSession;

import java.io.PrintStream;
import java.io.Reader;


/**
 * Read Eval Print Loop (REPL)
 */
public class Repl implements Runnable {

  private final Console console;
  private StudioSession session;
  private Context topLevelContext;
  
  private Environment global;
  
  
  public Repl(Console console, StudioSession session) throws FileSystemException {
    this.console = console;    
    this.session = session;
    this.topLevelContext = session.getTopLevelContext();
    this.global = topLevelContext.getEnvironment();
    
    
    if(console instanceof RichConsole) {
      ((RichConsole) console).setNameCompletion(new SymbolCompletion(global));
    }
  }
 
  
  @Override
  public void run() {

    ParseOptions options = ParseOptions.defaults();
    ParseState state = new ParseState();
    Reader reader = console.getIn();
    RLexer lexer = new RLexer(options, state, reader);
    RParser parser = new RParser(options, state, lexer);

    printGreeting();

    while(true) {

      console.getOut().print("> ");

      try {
        
        parser.parse();
    	  
        SEXP exp = parser.getResult();
        if(exp == null) {
          continue;
        }
        
        // clean up last warnings from any previous run
        clearWarnings();
        
        SEXP result = topLevelContext.evaluate(exp, global);

        if(!topLevelContext.getSession().isInvisible()) {
          topLevelContext.evaluate(FunctionCall.newCall(Symbol.get("print"), result));  
        }
        
        printWarnings();

      } catch (ParseException e) {
        console.getErr().println(String.format("Error: %s", e.getMessage()));

      } catch (EvalException e) {
        console.getErr().println(String.format("Error: %s", e.getMessage()));

      } catch (Exception e) {
        console.getErr().println(String.format("Java exception thrown: " + e.getMessage()));
        e.printStackTrace();
      }
    }
  }

  private void printWarnings() {
    SEXP warnings = topLevelContext.getBaseEnvironment().getVariable(Warning.LAST_WARNING);
    if(warnings != Symbol.UNBOUND_VALUE) {
      topLevelContext.evaluate( FunctionCall.newCall(Symbol.get("print.warnings"), warnings),
          topLevelContext.getBaseEnvironment());
      
      console.getOut().println();
    }
  }

  private void clearWarnings() {
    topLevelContext.getBaseEnvironment().remove(Warning.LAST_WARNING);
  }

  private void printGreeting() {
    PrintStream out = console.getOut();
    out.print("Renjin " + RenjinVersion.getVersionName() + "\n");

    out.print("Copyright (C) 2013 The R Foundation for Statistical Computing\n");
    out.print("Copyright (C) 2014 BeDataDriven\n");

  }

}
