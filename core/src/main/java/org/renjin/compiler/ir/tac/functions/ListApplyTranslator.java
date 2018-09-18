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
import org.renjin.compiler.cfg.InlinedFunction;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.ApplyExpression;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.eval.MatchedArguments;
import org.renjin.primitives.special.ListApplyFunction;
import org.renjin.sexp.*;

public class ListApplyTranslator extends FunctionCallTranslator {
  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {

    MatchedArguments matched = ListApplyFunction.MATCHER.match(call.getArguments());
    SEXP vector = matched.getActualForFormal(0);
    SEXP function = matched.getActualForFormal(1);

    InlinedFunction inlinedClosure = resolveApplyFunction(builder, call, function);

    return new ApplyExpression(builder.translateSimpleExpression(context, vector), inlinedClosure);
  }

  static InlinedFunction resolveApplyFunction(IRBodyBuilder builder, FunctionCall call, SEXP function) {

    String[] argumentNames = {null};

    if(isClosureDefinition(function)) {
      FunctionCall closureDef = (FunctionCall) function;
      PairList formals = closureDef.getArgument(0);
      SEXP body = closureDef.getArgument(1);
      Closure closure = new Closure(Environment.EMPTY, formals, body);
      return new InlinedFunction("FUN", builder.getRuntimeState(), closure, argumentNames);

    } else if(function instanceof Symbol) {
      Function resolvedFunction = builder.resolveFunction(function);
      if(resolvedFunction instanceof Closure) {

        Closure closure = (Closure) resolvedFunction;
        return new InlinedFunction(((Symbol) function).getPrintName(), builder.getRuntimeState(), closure, argumentNames);

      } else if(resolvedFunction instanceof PrimitiveFunction) {
        Symbol formal = Symbol.get("x");

        PairList formals = new PairList.Node(formal, Symbol.MISSING_ARG, Null.INSTANCE);
        FunctionCall body = FunctionCall.newCall(resolvedFunction, formal);
        Closure closure = new Closure(Environment.EMPTY, formals, body);
        return new InlinedFunction(((PrimitiveFunction) resolvedFunction).getName(), builder.getRuntimeState(), closure, argumentNames);
      }
    }
    throw new NotCompilableException(call);
  }

  public static boolean isClosureDefinition(SEXP function) {
    if(function instanceof FunctionCall) {
      FunctionCall call = (FunctionCall) function;
      if(call.getFunction() instanceof Symbol) {
        Symbol name = (Symbol) call.getFunction();
        if(name.getPrintName().equals("function")) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    throw new NotCompilableException(call);
  }
}
