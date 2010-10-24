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

package r.lang.primitive.types;

import r.lang.Logical;
import r.lang.RealExp;
import r.lang.SEXP;

import java.util.ArrayList;
import java.util.List;

import static r.lang.internal.c.RInternals.R_atof;

public class CoerceToRealVisitor extends AbstractCoerceVisitor<Double> {

  // TODO: wildy inefficient, work with arrays...

  private List<Double> values = new ArrayList<Double>();

  public CoerceToRealVisitor(SEXP exp) {
    exp.accept(this);
  }

  @Override
  protected Double coerce(Logical logical) {
    switch (logical) {
      case TRUE:
        return 1d;
      case FALSE:
        return 0d;
      case NA:
        return RealExp.NA_REAL;
    }
    throw new IllegalArgumentException();
  }

  @Override
  protected Double coerce(Double d) {
    return d;
  }

  @Override
  protected Double coerce(String s) {
    return R_atof(s);
  }

  @Override
  protected Double coerce(Integer i) {
    return (double)i;
  }

  @Override
  protected void collect(Double value) {
    values.add(value);
  }

  @Override
  public SEXP coerce() {
    return new RealExp(values);
  }
}
