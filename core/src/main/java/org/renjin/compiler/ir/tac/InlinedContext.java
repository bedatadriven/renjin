/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.compiler.ir.tac;

import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.functions.TranslationContext;
import org.renjin.eval.MatchedArgumentPositions;
import org.renjin.sexp.Logical;
import org.renjin.sexp.PairList;
import org.renjin.sexp.Symbol;

import java.util.List;


public class InlinedContext implements TranslationContext {

  private final PairList formals;
  private final MatchedArgumentPositions matched;
  private final List<IRArgument> extraArguments;

  public InlinedContext(PairList formals, MatchedArgumentPositions matched, List<IRArgument> extraArguments) {
    this.formals = formals;
    this.matched = matched;
    this.extraArguments = extraArguments;
  }

  @Override
  public boolean isEllipsesArgumentKnown() {
    return true;
  }

  @Override
  public List<IRArgument> getEllipsesArguments() {
    return extraArguments;
  }

  public PairList getFormals() {
    return formals;
  }

  @Override
  public Expression isMissing(Symbol name) {
    for (int formalIndex = 0; formalIndex < matched.getFormalCount(); formalIndex++) {
      if(matched.getFormalSymbol(formalIndex) == name) {
        new Constant(Logical.valueOf(matched.isFormalMatched(formalIndex)));
      }
    }
    throw new InvalidSyntaxException("'missing' can only used for arguments");
  }
}
