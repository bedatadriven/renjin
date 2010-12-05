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

import com.google.common.base.Preconditions;
import r.lang.exception.EvalException;

public class SymbolExp extends AbstractSEXP {
                            
  public static final int TYPE_CODE = 1;
  public static final String  TYPE_NAME = "symbol";
  public static final String IMPLICIT_CLASS = "name";


  public static final SymbolExp UNBOUND_VALUE = createUnbound();
  public static final SymbolExp MISSING_ARG = new SymbolExp();
  public static final SymbolExp NAMES = new SymbolExp("names");
  public static final SymbolExp CLASS = new SymbolExp("class") ;
  public static final SymbolExp STDOUT = new SymbolExp("stdout");
  public static final SymbolExp ELLIPSES = new SymbolExp("...");
  public static final SymbolExp SRC_REF = new SymbolExp("srcref");
  public static final SymbolExp SRC_FILE = new SymbolExp("srcfile");
  public static final SymbolExp TEMP_VAL = new SymbolExp("*tmp*");

  private String printName;

  private SymbolExp() {
  }

  public SymbolExp(String printName) {
    Preconditions.checkNotNull(printName);

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

  @Override
  protected final String getImplicitClass() {
    return IMPLICIT_CLASS;
  }

  public String getPrintName() {
    return printName;
  }

  private static SymbolExp createUnbound() {
    /* R_UnboundValue */
    SymbolExp instance = new SymbolExp();
    return instance;
  }

  @Override
  public EvalResult evaluate(EnvExp rho) {
    SEXP value = rho.findVariable(this);
    if(value == SymbolExp.UNBOUND_VALUE) {
      throw new EvalException(String.format("object '%s' not found", printName));
    }
    if(value instanceof PromiseExp) {
      return value.evaluate(rho);
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

    SymbolExp symbolExp = (SymbolExp) o;

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
