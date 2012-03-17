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

package r.lang.exception;

import r.lang.Context;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.Context.Type;

public class EvalException extends RuntimeException {
  private SEXP exp;
  private Context context;

  public EvalException(String message, Object... args) {
    super(args.length == 0 ? message : String.format(message, args));
  }

  public EvalException(String message, Throwable t, Object... args) {
      super(args.length == 0 ? message : String.format(message, args),t);
    }

  
  public EvalException(Context context, String message, Object... args) {
    super(String.format(message, args));
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

  @Override
  public String getMessage() {
    if(context == null) {
      return super.getMessage();
    }
    StringBuilder sb = new StringBuilder();
    if(super.getMessage() != null) {
      sb.append(super.getMessage());
    }
    Context context = this.context;
    sb.append("\nR Stack Trace:");
    while(!context.isTopLevel()) {
      if(context.getType() == Type.FUNCTION) {
        sb.append("\n  at ").append(context.getFunctionName());
        appendArguments(sb, context);
      }
      context = context.getParent();
    }
    sb.append("\nJava Stack Trace:\n");
    return sb.toString();
  }

  private void appendArguments(StringBuilder sb, Context context) {
    boolean needsComma=false;
    sb.append("(");
    for(PairList.Node node : context.getArguments().nodes()) {
      if(needsComma) {
        sb.append(",");
      } else {
        needsComma=true;
      }
      if(node.hasTag()) {
        sb.append(node.getTag()).append("=");
      }
      sb.append(node.getValue());
    }
    sb.append(")");
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
}
