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

package r.compiler.ir.tree.builders;

import r.compiler.ReservedWords;
import r.compiler.ir.temp.Label;
import r.compiler.ir.tree.*;
import r.lang.FunctionCall;
import r.lang.SEXP;

public class IfStatement implements ReservedWords.IRBuilder {
  @Override
  public Statement buildStm(IRFactory factory, FunctionCall call) {
    Label trueLabel = factory.newLabel();
    Label doneLabel = factory.newLabel();
    return
        new SeqStm(
            new CJumpStm(factory.buildExp(call.<SEXP>getArgument(0)), trueLabel, doneLabel),
               new SeqStm(new LabelStm(trueLabel),
                   new SeqStm(factory.buildStm(call.getArgument(1)),
                     new LabelStm(doneLabel))));
  }

  @Override
  public Exp buildExp(IRFactory factory, FunctionCall call) {
    throw new UnsupportedOperationException();
  }
}
