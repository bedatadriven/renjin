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

import org.apache.commons.math.complex.Complex;
import r.lang.*;

public abstract class AbstractProgram implements Program {

  protected static NilExp NULL = NilExp.INSTANCE;
  protected static SymbolExp MISSING = SymbolExp.MISSING_ARG;


  // these constants are defined here for the convenience of the 
  // code generation
  protected static String NA_character_ = StringExp.NA;
  protected static double NA_real_ = RealExp.NA;
  protected static double NaN = Double.NaN;
  protected static double Inf = Double.POSITIVE_INFINITY;

  protected AbstractProgram() {
  }

  protected LangExp call(SEXP function, PairList arguments) {
    return new LangExp(function, arguments);
  }

  protected RealExp c(double... d) {
    return new RealExp(d);
  }

  protected IntExp c_int(int... i) {
    return new IntExp(i);
  }

  protected StringExp c(String... s) {
    return new StringExp(s);
  }

  protected LogicalExp c(Logical... v) {
    return new LogicalExp(v);
  }

  protected ListExp list(SEXP... items ) {
    return ListExp.fromArray(items);
  }
  
  protected ComplexExp c(Complex... complex) {
    return new ComplexExp(complex);
  }
}
