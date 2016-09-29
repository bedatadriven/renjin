/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
import org.renjin.compiler.ir.tac.InlinedContext;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.UseMethodCall;
import org.renjin.sexp.*;

/**
 * Handles a call to UseMethod
 */
public class UseMethodTranslator extends FunctionCallTranslator {
  @Override
  public Expression translateToExpression(IRBodyBuilder builder, 
                                          TranslationContext context, 
                                          Function resolvedFunction, 
                                          FunctionCall call) {
    
    if(!(context instanceof InlinedContext)) {
      throw new InvalidSyntaxException("'UseMethod' used in an inappropriate fashion.");
    }

    InlinedContext inlinedContext = (InlinedContext) context;
    
    int arity = call.getArguments().length();
    
    // First argument is the name of the generic method, and is required.
    if(arity < 1) {
      throw new InvalidSyntaxException("There must be a 'generic' argument");
    }
    
    SEXP genericSexp = call.getArgument(0);
    if(!(genericSexp instanceof StringVector) || genericSexp.length() != 1) {
      throw new InvalidSyntaxException("'generic' must be a character string");
    }
    String generic = ((StringVector) genericSexp).getElementAsString(0);

    // Next we need the object to dispatch on. If omitted, the value of the first 
    // FORMAL is used. 
    Expression objectExpr;
    if(arity == 1) {
      PairList formals = inlinedContext.getFormals();
      if(formals == Null.INSTANCE) {
        objectExpr = new Constant(Null.INSTANCE);
      } else {
        Symbol formalName = formals.getTag();
        if(formalName == Symbols.ELLIPSES) {
          throw new NotCompilableException(call, "UseMethod() not supported when first argument to function is '...'");
        } else {
          objectExpr = new EnvironmentVariable(formalName);
        }
      }
    } else {
      throw new NotCompilableException(call);
    }
    
    if(inlinedContext.getFormals().length() > 1) {
      throw new NotCompilableException(call, "UseMethod() not yet supported when more than one argument is present.");
    }
    
    return new UseMethodCall(builder.getRuntimeState(), call, generic, objectExpr);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    throw new NotCompilableException(call);
  }
}
