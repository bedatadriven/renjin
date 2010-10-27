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

package r.compiler.runtime;

import r.lang.*;

public abstract class AbstractProgram implements Program {

  protected static NilExp NULL = NilExp.INSTANCE;
  protected static SymbolExp MISSING = SymbolExp.MISSING_ARG;

  protected AbstractProgram() {
  }

  protected LangExp call(SEXP function, PairList arguments) {
    return new LangExp(function, arguments);
  }

  protected RealExp c(double... d) {
    return new RealExp(d);
  }

  protected IntExp ci(int... i) {
    return new IntExp(i);
  }

  protected StringExp c(String... s) {
    return new StringExp();
  }

  protected ListExp list(SEXP... items ) {
    return ListExp.fromArray(items);
  }
  
}
