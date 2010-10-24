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

  public SEXP R_CurrentExpr;

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

  public SymbolTable getSymbolTable() {
    return symbolTable;
  }

  public void setSymbolTable(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
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
}
