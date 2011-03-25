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

package r.compiler.ir.tree;

import r.compiler.ir.temp.Label;

public class CJumpStm extends Statement {
  public Exp exp;
  public Label trueLabel;
  public Label falseLabel;

  public CJumpStm(Exp exp, Label trueLabel, Label falseLabel) {
    this.exp = exp;
    this.trueLabel = trueLabel;
    this.falseLabel = falseLabel;
  }

  public CJumpStm(Exp exp, Label trueLabel) {
    this.exp = exp;
    this.trueLabel = trueLabel;
    this.falseLabel = null;
  }

  @Override
  public String toString() {
    return "CJUMP(" + exp.toString() + ", " + trueLabel + ", " + falseLabel + ")";
  }
}
