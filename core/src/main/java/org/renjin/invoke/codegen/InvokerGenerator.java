/**
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
package org.renjin.invoke.codegen;


import com.sun.codemodel.*;
import org.renjin.eval.Context;
import org.renjin.invoke.model.PrimitiveModel;
import org.renjin.sexp.*;

import java.io.IOException;

public class InvokerGenerator {

  private final JCodeModel codeModel;

  public InvokerGenerator(JCodeModel codeModel) {
    this.codeModel = codeModel;
  }

  public final void generate(PrimitiveModel model) throws JClassAlreadyExistsException, IOException {
    JDefinedClass invoker = codeModel._class(  WrapperGenerator2.toFullJavaName(model.getName()) ); //Creates a new class
    if(model.isSpecial()) {
      invoker._extends(SpecialFunction.class);
    } else {
      invoker._extends(BuiltinFunction.class);
    }

    JMethod defaultConstructor = invoker.constructor(JMod.PUBLIC);
    defaultConstructor.body().invoke("super").arg(JExpr.lit(model.getName()));

    if(model.hasVargs() && model.getOverloads().size() > 1) {
      throw new GeneratorDefinitionException(model.getName() + ": If var args are used, multiple overloads cannot be used");
    }

    if(model.hasVargs()) {
      VarArgApplyBuilder apply = new VarArgApplyBuilder(codeModel, invoker, model);
      apply.build();

      ApplyArrayArgsMethodBuilder applyWithArray = new ApplyArrayArgsMethodBuilder(codeModel, invoker, model);
      applyWithArray.buildVarArgs();
      addArrayApplyOverload(invoker);

    } else {
      FixedArityApplyBuilder apply = new FixedArityApplyBuilder(codeModel, invoker, model);
      apply.build();

      ApplyArrayArgsMethodBuilder applyWithArray = new ApplyArrayArgsMethodBuilder(codeModel, invoker, model);
      applyWithArray.build();
      addArrayApplyOverload(invoker);

      for(Integer arity : model.getArity()) {
        OverloadWrapperBuilder doApply = new OverloadWrapperBuilder(codeModel, invoker, model, arity);
        doApply.build();
      }
    }
  }

  private void addArrayApplyOverload(JDefinedClass invoker) {
    JMethod method = invoker.method(JMod.PUBLIC, SEXP.class, "apply");
    JVar context = method.param(Context.class, "context");
    JVar environment = method.param(Environment.class, "environment");
    JVar call = method.param(FunctionCall.class, "call");
    JVar argNames = method.param(String[].class, "argNames");
    JVar args = method.param(SEXP[].class, "args");

    method.body()._return(invoker.staticInvoke("doApply")
            .arg(context)
            .arg(environment)
            .arg(call)
            .arg(argNames)
            .arg((args)));
  }
}
