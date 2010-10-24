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

class CombineToList extends SexpVisitor implements CoercingVisitor {

  ListExp.Builder builder = new ListExp.Builder();

  CombineToList(SEXP exp) {
    exp.accept(this);
  }

  @Override
  public void visit(ListExp listExp) {
    for(SEXP exp : listExp) {
      exp.accept(this);
    }
  }

  @Override
  public void visit(RealExp realExp) {
    for(Double d : realExp) {
      addElement(new RealExp(d));
    }
  }

  @Override
  public void visit(StringExp stringExp) {
    for(String s : stringExp) {
      addElement(new StringExp(s));
    }
  }

  @Override
  public void visit(IntExp intExp) {
    for(Integer i : intExp) {
      addElement(new IntExp(i));
    }
  }

  @Override
  public void visit(NilExp nilExp) {
    // Ignore
  }

  @Override
  protected void unhandled(SEXP exp) {
    addElement(exp);
  }

  private void addElement(SEXP exp) {
    builder.add(exp);
  }

  @Override
  public SEXP coerce() {
    return builder.list();
  }

}
