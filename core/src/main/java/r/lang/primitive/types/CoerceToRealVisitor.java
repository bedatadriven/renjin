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

import r.lang.*;
import r.parser.ParseUtil;

import java.util.ArrayList;

public class CoerceToRealVisitor extends SexpVisitor implements CoercingVisitor {

  private ArrayList<Double> values = new ArrayList<Double>();

  public CoerceToRealVisitor(SEXP exp) {
    exp.accept(this);
  }

  @Override
  public void visit(RealExp realExp) {
    values.addAll(realExp.asListOfDoubles());
  }

  @Override
  public void visit(IntExp intExp) {
    for(Integer i : intExp) {
      values.add(i.doubleValue());
    }
  }

  @Override
  public void visit(StringExp stringExp) {
    for(String s : stringExp) {
      values.add(ParseUtil.parseDouble(s));
    }
  }

  @Override
  public void visit(LogicalExp logicalExp) {
    for(Logical logical : logicalExp) {
      values.add(toDouble(logical));
    }
  }

  @Override
  public void visit(ListExp listExp) {
    for(SEXP s : listExp) {
      s.accept(this);
    }
  }

  @Override
  public void visit(NilExp nilExp) {
    // Ignore
  }

  private Double toDouble(Logical logical) {
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
  public SEXP coerce() {
    return new RealExp(values);
  }
}
