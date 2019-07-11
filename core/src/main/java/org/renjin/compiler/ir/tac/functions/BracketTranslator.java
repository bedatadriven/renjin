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


import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.sexp.*;


public class BracketTranslator extends FunctionCallTranslator {

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    ListVector sourceRefVector = null;
    SEXP sourceRefAttribute = call.getAttribute(Symbol.get("srcref"));
    if(sourceRefAttribute instanceof ListVector) {
      sourceRefVector = (ListVector) sourceRefAttribute;
    }

    if(call.getArguments().length() == 0) {
      return Constant.NULL;
    } else {
      int statementIndex = 0;
      for(PairList.Node arg : call.getArguments().nodes()) {

        if(sourceRefVector != null) {
          IntVector sourceRef = (IntVector) sourceRefVector.get(statementIndex);
          builder.recordLineNumber(sourceRef.getElementAsInt(0) + 1);
        }

        if(arg.hasNextNode()) {
          builder.translateStatements(context, arg.getValue()); 
        } else {
          return builder.translateExpression(context, arg.getValue());
        }

        statementIndex++;
      }
      throw new Error("unreachable");
    }
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    if(call.getArguments() != Null.INSTANCE) {
      for(SEXP arg : call.getArguments().values()) {
        builder.translateStatements(context, arg);
      }
    }
  }
}
