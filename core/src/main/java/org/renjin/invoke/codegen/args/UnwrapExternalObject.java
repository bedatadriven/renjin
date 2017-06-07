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
import org.renjin.invoke.model.JvmMethod.Argument;
import org.renjin.sexp.ExternalPtr;

import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.invoke;


public class UnwrapExternalObject extends ArgConverterStrategy {

  public UnwrapExternalObject(Argument formal) {
    super(formal);
  }

  public static boolean accept(Argument formal) {
    return !formal.getClazz().isPrimitive();
  }

  @Override
  public JExpression getTestExpr(JCodeModel codeModel, JVar sexp) {
    JClass externalClass = codeModel.ref(ExternalPtr.class);
    return sexp._instanceof(externalClass)
            .cand(invoke(cast(externalClass, sexp), "getInstance")._instanceof(codeModel.ref(formal.getClazz())));
  }

  @Override
  public JExpression convertArgument(ApplyMethodContext method, JExpression sexp) {
    JClass externalClass = method.classRef(ExternalPtr.class);
    JClass formalClass = method.classRef(formal.getClazz());
    return cast(formalClass, invoke(cast(externalClass, sexp), "getInstance"));
  }
}
