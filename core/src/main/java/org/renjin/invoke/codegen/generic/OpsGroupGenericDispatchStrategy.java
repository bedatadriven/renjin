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
package org.renjin.invoke.codegen.generic;

import com.sun.codemodel.*;
import org.renjin.invoke.codegen.ApplyMethodContext;
import org.renjin.primitives.S3;
import org.renjin.sexp.SEXP;

import java.util.List;

import static com.sun.codemodel.JExpr.lit;

/**
 * The 'Ops' group requires special treatment because they are always unary or binary,
 * and dispatch on either the first or second argument, which are always evaluated.
 */
public class OpsGroupGenericDispatchStrategy extends GenericDispatchStrategy {

  private final String name;

  public OpsGroupGenericDispatchStrategy(JCodeModel codeModel, String name) {
    super(codeModel);
    this.name = name;
  }


  @Override
  public void beforeTypeMatching(ApplyMethodContext context,
                                 JExpression functionCall,
                                 List<JExpression> arguments,
                                 JVar argNamesArray, JVar argsArray, JBlock parent) {

    JInvocation dispatchInvocation = codeModel.ref(S3.class)
            .staticInvoke("tryDispatchFromPrimitive")
              .arg(context.getContext())
              .arg(context.getEnvironment())
              .arg(functionCall)
              .arg(lit("Ops"))
              .arg(lit(name))
              .arg(argNamesArray)
              .arg(argsArray);

    JBlock ifObjects = parent._if(anyObjects(arguments))._then();
    JVar dispatchResult = ifObjects.decl(codeModel.ref(SEXP.class), "genericResult", dispatchInvocation);
    ifObjects._if(dispatchResult.ne(JExpr._null()))._then()._return(dispatchResult);
  }

  private JExpression anyObjects(List<JExpression> arguments) {
    if(arguments.size() == 1) {
      return fastIsObject(arguments.get(0));
    } else if(arguments.size() == 2) {
      return fastIsObject(arguments.get(0)).cor(fastIsObject(arguments.get(1)));
    } else {
      throw new UnsupportedOperationException("n arguments = " + arguments.size());
    }
  }
}

