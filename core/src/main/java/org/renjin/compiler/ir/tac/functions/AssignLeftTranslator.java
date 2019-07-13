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
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.eval.EvalException;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;


public class AssignLeftTranslator extends FunctionCallTranslator {

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {


    Expression rhs = builder.translateExpression(context, call.getArgument(1));

    // since the rhs will be used as this expression's value, 
    // we need to assure that it is not evaluated twice
    if(!(rhs instanceof Constant)) {
      // avoid evaluating RHS twice
      Temp temp = builder.newTemp();
      builder.addStatement(new Assignment(temp, rhs));
      rhs = temp;
    }
    
    addAssignment(builder, context, call, rhs);
    return rhs;
  }
  
  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall assignment) {
    Expression rhs = builder.translateExpression(context, assignment.getArgument(1));

    addAssignment(builder, context, assignment, rhs);
  }
  
  private void addAssignment(IRBodyBuilder builder, TranslationContext context, FunctionCall assignment, 
      Expression rhs) {
    // this loop handles nested, complex assignments, such as:
    // class(x) <- "foo"
    // x$a[3] <- 4
    // class(x$a[3]) <- "foo"

    SEXP lhs = assignment.getArgument(0);
    
    while(lhs instanceof FunctionCall) {
      FunctionCall call = (FunctionCall) lhs;
      
      rhs = builder.translateSetterCall(context, call, rhs);
      lhs = call.getArgument(0);
    }

    doAssignment(context, builder, lhs, rhs);

  }

  protected void doAssignment(TranslationContext context, IRBodyBuilder builder, SEXP lhs, Expression rhs) {

    LValue target;
    if( lhs instanceof Symbol) {
      target = builder.getEnvironmentVariable((Symbol)lhs);
    } else if(lhs instanceof StringVector) {
      target =  builder.getEnvironmentVariable( Symbol.get(((StringVector) lhs).getElementAsString(0)) );
    } else {
      throw new EvalException("cannot assign to value of type " + lhs.getTypeName());
    }

    // make the final assignment to the target symbol
    builder.addStatement(new Assignment(target, rhs));

    // If this symbol is used in a function call, we have to conditionally assign it
    // to the starred version.
    if(lhs instanceof Symbol && context.isUsedInFunctionCall((Symbol) lhs)) {
      FunctionRef functionRef = new FunctionRef((Symbol) lhs);

      builder.addStatement(new Assignment(functionRef, new PiFunction(functionRef, target)));
    }
  }

}
