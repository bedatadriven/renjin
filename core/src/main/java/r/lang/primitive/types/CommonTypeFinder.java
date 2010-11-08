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

/**
 * Finds the common type of an expression
 */
class CommonTypeFinder extends SexpVisitor {

  private boolean canBeReal = true;
  private boolean canBeInt = true;
  private boolean canBeLogical = true;
  private boolean canBeString = true;

  /**
   * Visits each element of {@code ListExp}
   */
  CommonTypeFinder(ListExp listExp) {
     for(SEXP exp : listExp) {
      exp.accept(this);
    }
  }

  @Override
  public void visit(DoubleExp realExp) {
    canBeInt = false;
    canBeLogical = false;
  }

  @Override
  public void visit(IntExp intExp) {
    canBeLogical = false;
  }

  @Override
  public void visit(LogicalExp logicalExp) {
  }

  @Override
  public void visit(NullExp nilExp) {
    // ignore
  }

  @Override
  public void visit(StringExp stringExp) {
    canBeInt = false;
    canBeReal = false;
    canBeLogical = false;
  }

  @Override
  protected void unhandled(SEXP exp) {
    canBeInt = false;
    canBeReal = false;
    canBeString = false;
    canBeLogical = false;
  }

  /**
   * @return the common type of the visited expressions
   */
  public Class<? extends SEXP> get() {
    if(canBeLogical) {
      return LogicalExp.class;

    } else if(canBeInt) {
      return IntExp.class;

    } else if(canBeReal) {
      return DoubleExp.class;

    } else if(canBeString) {
      return StringExp.class;

    } else {
      return ListExp.class;
    }
  }
}
