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
import org.renjin.invoke.codegen.VarArgParser;
import org.renjin.primitives.S3;
import org.renjin.sexp.SEXP;

import static com.sun.codemodel.JExpr.lit;

public class SummaryGroupGenericStrategy extends GenericDispatchStrategy {

  private final String name;

  public SummaryGroupGenericStrategy(JCodeModel codeModel, String name) {
    super(codeModel);
    this.name = name;
  }


  @Override
  public void beforePrimitiveCalled(JBlock parent, VarArgParser args, ApplyMethodContext context, JExpression call, JVar argNamesArray, JVar argsArray) {
    JBlock isObject = parent._if(args.getVarArgBuilder().invoke("length").gt(lit(0))
            .cand(fastIsObject(args.getVarArgList().invoke("getElementAsSEXP").arg(lit(0)))))._then();
    JVar genericResult = isObject.decl(codeModel.ref(SEXP.class), "genericResult",
            codeModel.ref(S3.class)
                    .staticInvoke("tryDispatchFromSummary")
                    .arg(context.getContext())
                    .arg(context.getEnvironment())
                    .arg(call)
                    .arg("Summary")
                    .arg(lit(name))
                    .arg(argNamesArray)
                    .arg(argsArray));
    isObject._if(genericResult.ne(JExpr._null()))
            ._then()
            ._return(genericResult);
  }



}
