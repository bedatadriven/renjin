/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package r.lang;

import r.compiler.runtime.Program;
import r.parser.RParser;

import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * There is lots of state that is shared globally;
 * we want to encapsulate this in a class so that we can have multiple R interpreters
 * in the same thread
 */
public class GlobalContext {

  private Logger logger = Logger.getLogger("R");

  private SymbolTable symbolTable = new SymbolTable();
  private BaseEnvExp baseEnvironment;
  private EnvExp globalEnvironment;
  private PrintStream printStream;

  public GlobalContext() {

    /*
     * R maintains a chain of Environments, at the root
     * of which is the base environment.
     *
     * We start out with the GlobalEnvironment at the bottom, but
     * greater depth due to function closures.
     *
     * Other environments - such as packages -- can be "attached"
     * and are inserted between the global and base environments.
     */

    baseEnvironment = new BaseEnvExp(this, symbolTable);
    globalEnvironment = new EnvExp(baseEnvironment);
  }

  /**
   * Loads the base package in to the base environment.
   *
   * Note that this not required if your just compiling.
   */
  public void loadBasePackage() {
    // we have to use reflection to load the base library because it's
    // not actually compiled until after we're compiled.

    Program program;
    try {
      Class programClass = Class.forName("r.base.Base");
      program = (Program) programClass.newInstance();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("The Base package (r.base.Base) could not be found on the classpath.", e);
    } catch (InstantiationException e) {
      throw new RuntimeException("Could not create the Base package", e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Exception thrown while instantiating the base package", e);
    }
    program.evaluate(baseEnvironment);
  }

  public SymbolTable getSymbolTable() {
    return symbolTable;
  }

  public EnvExp getBaseEnvironment() {
    return baseEnvironment;
  }

  public EnvExp getGlobalEnvironment() {
    return globalEnvironment;
  }

  public Iterable<EnvExp> environments() {
    return globalEnvironment.selfAndParents();
  }

  public void warningCall(LangExp call, String message) {
    logger.warning(message);
  }

  /**
   * Convenience method for parsing and evaluating
   * R language statements
   *
   * @param source R language statements
   * @return
   */
  public SEXP evaluate(String source) {
    return RParser.parseSource(this, source).evalToExp(globalEnvironment);
  }

  /**
   * Convenience method for {@code getSymbolTable().install() }
   * @param name symbol name to retrieve/install in symbol table
   * @return
   */
  public SymbolExp symbol(String name) {
    return getSymbolTable().install(name);
  }

  public PrintStream getPrintStream() {
    return printStream;
  }

  public void setPrintStream(PrintStream printStream) {
    this.printStream = printStream;
  }
}
