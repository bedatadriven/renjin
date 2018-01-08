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

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import org.renjin.invoke.codegen.ApplyMethodContext;
import org.renjin.primitives.S3;
import org.renjin.sexp.SEXP;

import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr.lit;

/**
 * Dispatches a S3 generic 
 */
public class GroupDispatchStrategy extends GenericDispatchStrategy {

  private String groupName;
  private final String methodName;

  public GroupDispatchStrategy(JCodeModel codeModel, String groupName, String methodName) {
    super(codeModel);
    this.groupName = groupName;
    this.methodName = methodName;
  }

  @Override
  public void afterFirstArgIsEvaluated(ApplyMethodContext context, JExpression functionCall, JExpression arguments,
                                       JBlock parent, JExpression argument) {

    JBlock ifObject = parent._if(fastIsObject(argument))._then();
    JExpression genericResult = ifObject.decl(codeModel.ref(SEXP.class), "genericResult",
        codeModel.ref(S3.class).staticInvoke("tryDispatchGroupFromPrimitive")
            .arg(context.getContext())
            .arg(context.getEnvironment())
            .arg(functionCall)
            .arg(lit(groupName))
            .arg(lit(methodName))
            .arg(argument)
            .arg(arguments));
    ifObject._if(genericResult.ne(_null()))._then()._return(genericResult);
  }


}
