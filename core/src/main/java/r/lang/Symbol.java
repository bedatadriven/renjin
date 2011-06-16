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

package r.lang;

import com.google.common.base.Preconditions;
import r.lang.exception.EvalException;

public class Symbol extends AbstractSEXP {

  public static final String  TYPE_NAME = "symbol";
  public static final String IMPLICIT_CLASS = "name";

  public static final Symbol UNBOUND_VALUE = createSpecial();
  public static final Symbol MISSING_ARG = createSpecial();
  public static final Symbol NAMES = new Symbol("names");
  public static final Symbol DIM = new Symbol("dim");
  public static final Symbol CLASS = new Symbol("class");
  public static final Symbol LEVELS = new Symbol("levels");
  public static final Symbol STDOUT = new Symbol("stdout");
  public static final Symbol ELLIPSES = new Symbol("...");
  public static final Symbol SRC_REF = new Symbol("srcref");
  public static final Symbol SRC_FILE = new Symbol("srcfile");
  public static final Symbol TEMP_VAL = new Symbol("*tmp*");
  public static final Symbol DIMNAMES = new Symbol("dimnames");
  public static final Symbol NAME = new Symbol("name");
  public static final Symbol DOT_ENVIRONMENT = new Symbol(".Environment");
  public static final Symbol ROW_NAMES =  new Symbol("row.names");
  public static final Symbol TEMP = new Symbol("*tmp*");
  public static final Symbol AS_CHARACTER = new Symbol("as.character");

  private String printName;

  private Symbol() {
  }

  public Symbol(String printName) {
    Preconditions.checkNotNull(printName);

    this.printName = printName;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  protected final String getImplicitClass() {
    return IMPLICIT_CLASS;
  }

  public String getPrintName() {
    return printName;
  }

  private static Symbol createSpecial() {
    /* R_UnboundValue */
    return new Symbol() {
      @Override
      public int hashCode() {
        return 0;
      }

      @Override
      public boolean equals(Object o) {
        return this == o;
      }
    };
  }

  @Override
  public EvalResult evaluate(Context context, Environment rho) {
    SEXP value = rho.findVariable(this);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException(String.format("object '%s' not found", printName));
    }
    if(value instanceof Promise) {
      return value.evaluate(context, rho);
    } else {
      return new EvalResult(value);
    }
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Symbol symbolExp = (Symbol) o;

    if (!printName.equals(symbolExp.printName)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return printName.hashCode();
  }

  @Override
  public String toString() {
    if (this == UNBOUND_VALUE) {
      return "<unbound>";
    } else if (this == MISSING_ARG) {
      return "<missing_arg>";
    } else {
      return getPrintName();
    }
  }
}
