/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.annotations.processor.generic;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import org.renjin.primitives.annotations.processor.ApplyMethodContext;
import org.renjin.primitives.annotations.processor.WrapperRuntime;
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
  public void afterArgIsEvaluated(ApplyMethodContext context, JExpression functionCall, JExpression arguments,
                                  JBlock parent, JExpression argument, int index) {
    if(index == 0) {

      JBlock ifObject = parent._if(fastIsObject(argument))._then();
      JExpression genericResult = ifObject.decl(codeModel.ref(SEXP.class), "genericResult",
              codeModel.ref(WrapperRuntime.class).staticInvoke("tryDispatchFromPrimitive")
              .arg(context.getContext())
              .arg(context.getEnvironment())
              .arg(functionCall)
              .arg(lit(name))
              .arg(argument)
              .arg(arguments));
      ifObject._if(genericResult.ne(_null()))._then()._return(genericResult);

    }
  }

}
