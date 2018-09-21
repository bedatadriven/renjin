/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.sexp.*;

public class SubstituteTranslator extends FunctionCallTranslator {


  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
                                          TranslationContext context,
                                          Function resolvedFunction,
                                          FunctionCall call) {

    // Handle the special case of substitute(list(...))
    if(listEllipses(call)) {
      FunctionCall.Builder result = new FunctionCall.Builder();
      result.add(Symbol.get("list"));
      for (IRArgument argument : context.getEllipsesArguments()) {
        result.add(argument.getName(), argument.getSexp());
      }
      return new Constant(result.build());
    }

    throw new NotCompilableException(call);
  }

  private boolean listEllipses(FunctionCall call) {
    if(call.getArguments().length() == 1) {
      PairList.Node argument = (PairList.Node) call.getArguments();
      if(argument.getRawTag() == Null.INSTANCE) {
        if(argument.getValue() instanceof FunctionCall) {
          FunctionCall functionCall = (FunctionCall) argument.getValue();
          if(functionCall.getFunction() == Symbol.get("list")) {
            if(functionCall.getArguments().length() == 1) {
              if(functionCall.getArgument(0) == Symbols.ELLIPSES) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    // NOOP
  }
}
