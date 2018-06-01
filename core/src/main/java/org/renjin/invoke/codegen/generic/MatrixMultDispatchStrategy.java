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
package org.renjin.invoke.codegen.generic;

import com.sun.codemodel.*;
import org.renjin.invoke.codegen.ApplyMethodContext;
import org.renjin.primitives.Types;
import org.renjin.s4.S4;
import org.renjin.sexp.SEXP;

import java.util.List;

/**
 * Handles generic dispatch for matrix multiplication, which ONLY dispatches
 * if any of the arguments are S4.
 */
public class MatrixMultDispatchStrategy extends GenericDispatchStrategy {
  public MatrixMultDispatchStrategy(JCodeModel codeModel) {
    super(codeModel);
  }


  @Override
  public void beforeTypeMatching(ApplyMethodContext context,
                                 JExpression functionCall, List<JExpression> arguments,
                                 JBlock parent) {

    // Call public static SEXP tryDispatchToS4Method(@Current Context context, SEXP source, PairList args,
    //                                           Environment rho, String group, String opName)

    JInvocation dispatchInvocation = codeModel.ref(S4.class)
        .staticInvoke("tryS4DispatchFromPrimitive")
        .arg(context.getContext())  // context
        .arg(arguments.get(0))      // source
        .arg(functionCall.invoke("getArguments"))           // args
        .arg(context.getEnvironment())
        .arg(JExpr._null())
        .arg(JExpr.lit("%*%"));


    JBlock ifObjects = parent._if(anyS4(arguments))._then();
    JVar dispatchResult = ifObjects.decl(codeModel.ref(SEXP.class), "genericResult", dispatchInvocation);
    ifObjects._if(dispatchResult.ne(JExpr._null()))._then()._return(dispatchResult);
  }

  private JExpression anyS4(List<JExpression> arguments) {
    if(arguments.size() == 1) {
      return isS4(arguments.get(0));
    } else if(arguments.size() == 2) {
      return isS4(arguments.get(0)).cor(isS4(arguments.get(1)));
    } else {
      throw new UnsupportedOperationException("n arguments = " + arguments.size());
    }
  }

  protected JInvocation isS4(JExpression argument) {
    // without the explicit cast to AbstractSEXP, the JVM will not inline the call to isObject
    // which has a drastic impact on performance
    return codeModel.ref(Types.class).staticInvoke("isS4").arg(argument);
  }

}
