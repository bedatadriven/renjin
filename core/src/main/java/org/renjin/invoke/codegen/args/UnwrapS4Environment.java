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

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.invoke.codegen.ApplyMethodContext;
import org.renjin.invoke.codegen.WrapperRuntime;
import org.renjin.invoke.model.JvmMethod.Argument;
import org.renjin.sexp.Environment;
import org.renjin.sexp.ExternalPtr;
import org.renjin.sexp.SEXP;

import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.invoke;

/**
 * Converts a formal argument of type 'Environment' via the
 * {@link WrapperRuntime#unwrapEnvironmentSuperClass(SEXP)} method, which either
 * casts the SEXP to an Environment, or accesses the environment super class from .xData slot of
 * an S4 Object.
 */
public class UnwrapS4Environment extends ArgConverterStrategy {

  public UnwrapS4Environment(Argument formal) {
    super(formal);
  }

  public static boolean accept(Argument formal) {
    return formal.getClazz().equals(Environment.class);
  }

  @Override
  public JExpression getTestExpr(JCodeModel codeModel, JVar sexp) {
    return codeModel
        .ref(WrapperRuntime.class)
        .staticInvoke("isEnvironmentOrEnvironmentSubclass")
        .arg(sexp);
  }

  @Override
  public JExpression convertArgument(ApplyMethodContext method, JExpression sexp) {
    return method.getCodeModel()
        .ref(WrapperRuntime.class)
        .staticInvoke("unwrapEnvironmentSuperClass")
        .arg(sexp);
  }
}
