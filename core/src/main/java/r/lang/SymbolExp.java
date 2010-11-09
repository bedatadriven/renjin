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

import r.lang.exception.EvalException;
import r.util.ArgChecker;

public class SymbolExp extends SEXP {
                            
  public static final int TYPE_CODE = 1;
  public static final String  TYPE_NAME = "symbol";

  public static final SymbolExp UNBOUND_VALUE = createUnbound();
  public static final SymbolExp MISSING_ARG = new SymbolExp();

  private String printName;
  private SEXP value = UNBOUND_VALUE;
  private SEXP internal = UNBOUND_VALUE;

  private SymbolExp() {
  }

  public SymbolExp(String printName) {
    ArgChecker.notNull(printName);

    this.printName = printName;
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  public String getPrintName() {
    return printName;
  }

  public SEXP getInternal() {
    return internal;
  }

  public void setInternal(SEXP internal) {
    this.internal = internal;
  }

  public SEXP getValue() {
    return value;
  }

  public void setValue(SEXP value) {
    this.value = value;
  }

  private static SymbolExp createUnbound() {
    /* R_UnboundValue */
    SymbolExp instance = new SymbolExp();
    instance.value = instance;
    return instance;
  }

  @Override
  public EvalResult evaluate(EnvExp rho) {
    SEXP value = rho.findVariable(this);
    if(value == SymbolExp.UNBOUND_VALUE) {
      throw new EvalException(String.format("object '%s' not found", printName));
    }
    return value.evaluate(rho);
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);

  }

  @Override
  public String toString() {
    if (this == UNBOUND_VALUE) {
      return "<unbound>";
    } else if (this == MISSING_ARG) {
      return "<missing_arg>";
    } else {
      return "<" + getPrintName() + ">";
    }
  }
}
