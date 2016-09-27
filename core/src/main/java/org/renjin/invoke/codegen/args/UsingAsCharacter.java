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
package org.renjin.invoke.codegen.args;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.invoke.annotations.InvokeAsCharacter;
import org.renjin.invoke.codegen.ApplyMethodContext;
import org.renjin.invoke.codegen.WrapperRuntime;
import org.renjin.invoke.model.JvmMethod.Argument;


public class UsingAsCharacter extends ArgConverterStrategy {

  public UsingAsCharacter(Argument formal) {
    super(formal);
  }

  public static boolean accept(Argument formal) {
    return formal.isAnnotatedWith(InvokeAsCharacter.class);
  }

  @Override
  public JExpression convertArgument(ApplyMethodContext parent, JExpression sexp) {
    return parent.classRef(WrapperRuntime.class).staticInvoke("invokeAsCharacter")
            .arg(parent.getContext())
            .arg(parent.getEnvironment())
            .arg(sexp);
  }

  @Override
  public JExpression getTestExpr(JCodeModel codeModel, JVar sexpVariable) {
    return JExpr.TRUE;
  }
}
