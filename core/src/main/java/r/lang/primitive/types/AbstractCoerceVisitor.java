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

public abstract class AbstractCoerceVisitor<T> extends SexpVisitor implements CoercingVisitor {

  @Override
  public void visit(StringExp stringExp) {
    for(String s : stringExp) {
      collect(coerce(s));
    }
  }

  @Override
  public void visit(IntExp intExp) {
    for(Integer i : intExp) {
      collect(coerce(i));
    }
  }

  public void visit(RealExp realExp) {
    for(Double d : realExp) {
      collect(coerce(d));
    }
  }

  @Override
  public void visit(LogicalExp logicalExp) {
    for(Logical logical : logicalExp) {
      collect(coerce(logical));
    }
  }

  @Override
  public void visit(NilExp nilExp) {
    // ignore
  }

  @Override
  public void visit(ListExp listExp) {
    for(SEXP exp : listExp) {
      exp.accept(this);
    }
  }

  protected abstract void collect(T value);
  protected abstract T coerce(Logical logical);
  protected abstract T coerce(Double d);
  protected abstract T coerce(String s);
  protected abstract T coerce(Integer i);
}
