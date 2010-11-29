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

package r.lang.exception;

import r.lang.IntExp;
import r.lang.LangExp;
import r.lang.SEXP;
import r.lang.SymbolExp;

/**
 * Wraps an {@link r.lang.exception.EvalException EvalException} and includes
 * a reference to the function call in which the EvalException was thrown. 
 */
public class FunctionCallException extends RuntimeException {

  private final LangExp call;

  public FunctionCallException(LangExp call, Exception e) {
    super(formatMessage(call, e), e);
    this.call = call;
  }

  private static String formatMessage(LangExp call, Exception e) {
    String message = e.getMessage();
    SEXP sexp = call.getAttribute(SymbolExp.SRC_REF);
    if(sexp instanceof IntExp) {
      SEXP srcfile = sexp.getAttribute(SymbolExp.SRC_FILE);
      message += " (" + srcfile + ": " + ((IntExp) sexp).getInt(0) + ")";
    }
    return message;
  }
}
