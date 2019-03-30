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
package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.sexp.*;

public class MissingTranslator extends FunctionCallTranslator {
  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {

    Symbol argumentName = parseArgumentName(call);
    return new Constant(Logical.valueOf(context.isMissing(argumentName)));
  }

  private Symbol parseArgumentName(FunctionCall call) {
    int nargs = call.getArguments().length();
    if(nargs != 1) {
      throw new InvalidSyntaxException(nargs + " arguments passed to 'missing' which require 1");
    }
    SEXP argument = call.getArgument(0);

    if (argument instanceof Symbol) {
      return (Symbol) argument;
    } else if (argument instanceof StringVector && argument.length() == 1) {
      String name = ((StringVector) argument).getElementAsString(0);
      return Symbol.get(name);
    }
    throw new NotCompilableException(call);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    // NOOP
  }
}
