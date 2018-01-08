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
package org.renjin.compiler;


import org.renjin.eval.Context;
import org.renjin.primitives.Deparse;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;

public class NotCompilableException extends RuntimeException {
  private SEXP sexp;

  public NotCompilableException(SEXP sexp) {
    super("unsupported expression");
    this.sexp = sexp;
  }

  public NotCompilableException(SEXP sexp, String message) {
    super(message);
    this.sexp = sexp;
  }

  public NotCompilableException(FunctionCall call, NotCompilableException cause) {
    super(" in " + call.getFunction(), cause);
  }

  public SEXP getSexp() {
    return sexp;
  }
  
  public NotCompilableException getCause() {
    return (NotCompilableException) super.getCause();
  }


  public String toString(Context context) {
    NotCompilableException e = this;
    StringBuilder s = new StringBuilder();
    while(e != null) {
      if(s.length() > 0) {
        s.append(" > ");
      }
      if(e.getSexp() != null) {
        s.append(Deparse.deparseExp(context, e.getSexp()));
      }
      if(e.getMessage() != null) {
        s.append(": ").append(e.getMessage());
      }
      e = e.getCause();
    }
    return s.toString();
  }
}
