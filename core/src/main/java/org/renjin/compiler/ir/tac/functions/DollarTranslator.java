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
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.NamedElementAccess;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;


public class DollarTranslator extends FunctionCallTranslator {

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
                           FunctionCall call) {

    builder.addStatement(new ExprStatement(translateToExpression(builder, context, call)));
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
                                          TranslationContext context, FunctionCall call) {
    Expression object = builder.translateExpression(context, call.getArgument(0));

    String name;
    SEXP nameArgument = call.getArgument(1);
    if(nameArgument instanceof Symbol) {
      name = ((Symbol) nameArgument).getPrintName();
    } else if(nameArgument instanceof StringVector) {
      name = ((StringVector) nameArgument).getElementAsString(0);
    } else {
      throw new NotCompilableException(call);
    }

    return new NamedElementAccess(object, call, name);
  }
}
