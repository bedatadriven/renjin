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
package org.renjin.invoke.codegen.generic;

import com.sun.codemodel.*;
import org.renjin.invoke.codegen.ApplyMethodContext;
import org.renjin.invoke.codegen.VarArgParser;
import org.renjin.sexp.AbstractSEXP;

import java.util.List;

import static com.sun.codemodel.JExpr.cast;


public class GenericDispatchStrategy {

  protected final JCodeModel codeModel;

  public GenericDispatchStrategy(JCodeModel codeModel) {
    this.codeModel = codeModel;
  }

  public void afterFirstArgIsEvaluated(ApplyMethodContext context, JExpression functionCall, JExpression arguments,
                                       JBlock parent, JExpression argument) {

  }

  public void beforeTypeMatching(ApplyMethodContext context, JExpression functionCall,
                                 List<JExpression> arguments, JBlock parent) {


  }

  protected JInvocation fastIsObject(JExpression argument) {
    // without the explicit cast to AbstractSEXP, the JVM will not inline the call to isObject
    // which has a drastic impact on performance
    return JExpr.invoke(cast(codeModel.ref(AbstractSEXP.class), argument), "isObject");
  }

  public void beforePrimitiveCalled(JBlock parent, VarArgParser args, ApplyMethodContext context, JExpression call) {

  }
}
