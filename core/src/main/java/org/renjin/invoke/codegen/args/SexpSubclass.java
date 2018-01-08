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
package org.renjin.invoke.codegen.args;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.invoke.codegen.ApplyMethodContext;
import org.renjin.invoke.model.JvmMethod.Argument;
import org.renjin.sexp.SEXP;


/**
 * Handles checked narrowing of types to declared subclass of SEXP
 * @author alex
 *
 */
public class SexpSubclass extends ArgConverterStrategy {

  public SexpSubclass(Argument formal) {
    super(formal);
  }

  public static boolean accept(Argument formal) {
    return SEXP.class.isAssignableFrom(formal.getClazz());
  }

  @Override
  public JExpression convertArgument(ApplyMethodContext parent, JExpression sexp) {
    return JExpr.cast(parent.classRef(formal.getClazz()), sexp);
  }

  @Override
  public JExpression getTestExpr(JCodeModel codeModel, JVar sexpVariable) {
    return sexpVariable._instanceof(codeModel._ref(formal.getClazz()));
  }
}
