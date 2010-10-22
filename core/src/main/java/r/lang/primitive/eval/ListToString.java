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

package r.lang.primitive.eval;

import r.lang.*;
import r.lang.exception.LanguageException;

public class ListToString extends SexpVisitor {

  private StringBuilder sb = new StringBuilder();

  @Override
  protected void unhandled(SEXP exp) {
    throw new LanguageException(String.format("cannot coerce type '%s' to vector of type 'character'",
        exp.getType().getName()));
  }

  @Override
  public void visit(IntExp intExp) {
    for(Integer i : intExp) {
      sb.append(i);
    }
  }

  @Override
  public void visit(LogicalExp logicalExp) {
    for(Logical l : logicalExp) {
      sb.append(l.toString());
    }
  }

  @Override
  public void visit(NilExp nilExp) {
    sb.append("NULL");
  }

  @Override
  public void visit(RealExp realExp) {
    for(Double r : realExp) {
      sb.append(Double.toString(r));
    }
  }

  @Override
  public void visit(StringExp stringExp) {
    for(String s : stringExp) {
      sb.append(s);
    }
  }

  @Override
  public void visit(CharExp charExp) {
    sb.append(charExp.getValue());
  }

  @Override
  public void visit(ListExp listExp) {
    for(SEXP exp : listExp) {
      exp.accept(this);
    }
  }

  @Override
  public String toString() {
    return sb.toString();
  }

  public static String apply(SEXP sexp) {
    ListToString visitor = new ListToString();
    sexp.accept(visitor);
    return visitor.toString();
  }
}
