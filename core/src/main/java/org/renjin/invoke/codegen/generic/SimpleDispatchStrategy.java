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

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import org.renjin.invoke.codegen.ApplyMethodContext;
import org.renjin.primitives.S3;
import org.renjin.sexp.SEXP;

import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr.lit;


public class SimpleDispatchStrategy extends GenericDispatchStrategy {

  private final String name;

  public SimpleDispatchStrategy(JCodeModel codeModel, String name) {
    super(codeModel);
    this.name = name;
  }


  @Override
  public void afterFirstArgIsEvaluated(ApplyMethodContext context,
                                       JExpression functionCall,
                                       JExpression firstArgument,
                                       JExpression argNamesArray,
                                       JExpression argsArray,
                                       JBlock parent) {

    JBlock ifObject = parent._if(fastIsObject(firstArgument))._then();
    JExpression genericResult = ifObject.decl(codeModel.ref(SEXP.class), "genericResult",
            codeModel.ref(S3.class).staticInvoke("tryDispatchFromPrimitive")
            .arg(context.getContext())
            .arg(context.getEnvironment())
            .arg(functionCall)
            .arg(lit(name))
            .arg(JExpr._null())
            .arg(argNamesArray)
            .arg(argsArray));
    ifObject._if(genericResult.ne(_null()))._then()._return(genericResult);

  }

}
