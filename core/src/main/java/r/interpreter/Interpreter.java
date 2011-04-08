/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.interpreter;

import r.base.Print;
import r.lang.Context;
import r.lang.Environment;
import r.lang.EvalResult;
import r.lang.SEXP;
import r.lang.exception.EvalException;
import r.lang.exception.FunctionCallException;
import r.lang.exception.ParseException;
import r.parser.ParseOptions;
import r.parser.ParseState;
import r.parser.RLexer;
import r.parser.RParser;

import java.io.IOException;
import java.io.Reader;

/**
 * Interactive interpreter for the R-language
 */
public class Interpreter implements Runnable {

  private final Console console;
  private Environment global;
  private Context topLevelContext;

  public Interpreter(Console console) {

    this.console = console;

    this.topLevelContext = Context.newTopLevelContext();
    this.global = topLevelContext.getEnvironment();
   // this.global.setPrintStream(console.getOut());

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

    try {
      topLevelContext.loadBasePackage();
      topLevelContext.executeStartupProfile();

    } catch (IOException e) {
      console.println("Error loading base package");
    }


    while(true) {

      console.print("> ");

      try {
        parser.parse();

        SEXP exp = parser.getResult();
        EvalResult result = exp.evaluate(topLevelContext, global);

        if(result.isVisible()) {
          console.println( Print.print(result.getExpression(), console.getCharactersPerLine()) );
        }        

      } catch (ParseException e) {
        console.println(String.format("Error: %s", e.getMessage()));

      } catch (FunctionCallException e) {
        console.println(String.format("Error in '<fixme>': %s", e.getMessage()));

      } catch (EvalException e) {
        console.println(String.format("Error: %s", e.getMessage()));

      } catch (Exception e) {
        console.println(String.format("Java exception thrown: " + e.getMessage()));
        e.printStackTrace();
      }
    }
  }

  private void printGreeting() {
    console.print("Renjin 0.0.1-SNAPSHOT\n");
    console.print("Copyright (C) 2010 The R Foundation for Statistical Computing\n");
    console.print("Copyright (C) 2010 bedatadriven\n");
    console.print("ISBN 3-900051-07-0\n\n");

    console.print("R is free software and comes with ABSOLUTELY NO WARRANTY. " +
      "You are welcome to redistribute it under certain conditions. " +
      "Type 'license()' or 'licence()' for distribution details.\n\n");

    console.print("R is a collaborative project with many contributors. " +
      "Type 'contributors()' for more information and " +
      "'citation()' on how to cite R or R packages in publications.\n\n");

//    console.print("Type 'demo()' for some demos, 'help()' for on-line help, or " +
//      "'help.start()' for an HTML browser interface to help. " +
//      "Type 'q()' to quit R.\n\n");
  }

}
