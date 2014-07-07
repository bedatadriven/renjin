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

package org.renjin.eval;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.renjin.eval.Context.Type;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Null;
import org.renjin.sexp.Vector;


public class EvalException extends RuntimeException {
  private SEXP condition;
  private Context context;
  
  public EvalException(String message, Throwable t) {
    super(message, t);
    ListVector.NamedBuilder condition = ListVector.newNamedBuilder();
    condition.add("message", this.getMessage());
    condition.setAttribute(Symbols.CLASS, new StringArrayVector("condition", "error", "simpleError"));
    this.condition = condition.build();
  }
  
  public EvalException(String message, Object... args) {
    this(args.length == 0 ? message : String.format(message, args), (Throwable)null);
  }

  
  public EvalException(Context context, String message, Object... args) {
    this(args.length == 0 ? message : String.format(message, args), (Throwable)null);
    this.context = context;
  }


  public EvalException(Throwable cause) {
    super(cause.getMessage(), cause);
  }

  public Context getContext() {
    return context;
  }

  public void initContext(Context context) {
    if(this.context == null) {
      this.context = context;
    }
  }

  public void printRStackTrace(PrintWriter writer) {

    if(this.context != null) {
      Context context = this.context;

      while(!context.isTopLevel()) {
        if(context.getType() == Type.FUNCTION) {
            writer.append("  at ").append(context.getFunctionName().toString()).append("()");
            SEXP callFile = context.getParent().getSrcFile();
            SEXP callSrcref = context.getParent().getSrcRef();
            if (callFile != Null.INSTANCE) {
               writer.append("  ");
               writer.append(callFile.toString());
               if (callSrcref != Null.INSTANCE) {
                 int lineNumber = ((Vector)callSrcref).getElementAsInt(0);
                 writer.append("##").append(Integer.toString(lineNumber));
               }
            }
            writer.append("\n");
        }
        context = context.getParent();
      }

      if (getCause()!=null) {
         writer.append("Caused by:");
         Throwable excause = getCause();
         if (excause instanceof EvalException) {
             ((EvalException)excause).printRStackTrace(writer);
         } else {
             excause.printStackTrace(writer);
         }
      }

    } else {
      printStackTrace(writer);
    }

  }
  
  public void printRStackTrace(PrintStream stream) {
    PrintWriter writer = new PrintWriter(stream);
    printRStackTrace(writer);
    writer.flush();
  }

  public static void check(boolean condition, String errorMessage, Object... args) {
    if(!condition) {
      throw new EvalException(errorMessage, args);
    }
  }

  public static <S extends SEXP> S checkedCast(SEXP argument) {
    try {
      return (S)argument;
    } catch (ClassCastException e) {
      throw new EvalException("invalid 'type' (%s) of argument", argument.getTypeName());
    }
  }

  public SEXP getCondition() {
    return condition;
  }
}
