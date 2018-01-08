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
package org.renjin.invoke.codegen;

import com.sun.codemodel.*;
import org.renjin.eval.EvalException;

import static com.sun.codemodel.JExpr._new;

public class ExceptionWrapper {

  private JTryBlock tryBlock;
  private JCodeModel codeModel;
  private JExpression context;

  public ExceptionWrapper(JCodeModel codeModel, JBlock parent, JExpression context) {
    this.codeModel = codeModel;
    this.context = context;
    tryBlock = parent._try();
  }

  public JBlock body() {
    return tryBlock.body();
  }

  public void catchEvalExceptions() {
    JCatchBlock catchBlock = tryBlock._catch(codeModel.ref(EvalException.class));
    JVar e = catchBlock.param("e");
    catchBlock.body().invoke(e, "initContext").arg(context);
    catchBlock.body()._throw(e);
  }

  public void catchRuntimeExceptions() {
    JCatchBlock catchBlock = tryBlock._catch(codeModel.ref(RuntimeException.class));
    JVar e = catchBlock.param("e");
    catchBlock.body()._throw(e);
  }

  public void catchExceptions() {
    JCatchBlock catchBlock = tryBlock._catch(codeModel.ref(Exception.class));
    JVar e = catchBlock.param("e");
    catchBlock.body()._throw(_new(codeModel.ref(EvalException.class)).arg(e));
  }

  public JCatchBlock _catch(JClass jClass) {
    return tryBlock._catch(jClass);
  }
}
