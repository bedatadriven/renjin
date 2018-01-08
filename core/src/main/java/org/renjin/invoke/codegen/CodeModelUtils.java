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
import org.renjin.invoke.model.JvmMethod;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;


public class CodeModelUtils {


  static void returnSexp(JVar context, JCodeModel codeModel, JBlock parent, JvmMethod overload, JInvocation invocation) {
    
    if(overload.getReturnType().equals(Void.TYPE)) {
      parent.add(invocation);
      parent.add(context.invoke("setInvisibleFlag"));
      parent._return(codeModel.ref(Null.class).staticRef("INSTANCE"));
    } else {
      JVar result = parent.decl(codeModel._ref(SEXP.class), "__return", convertResult(codeModel, overload, invocation));

      if(overload.isInvisible()) {
        parent.add(context.invoke("setInvisibleFlag"));
      }
      parent._return(result);
    }
  }

  static JExpression convertResult(JCodeModel codeModel, JvmMethod overload, JInvocation invocation) {
    if(SEXP.class.isAssignableFrom(overload.getReturnType())) {
      return invocation;
    } else {
      return codeModel.ref(WrapperRuntime.class).staticInvoke("wrapResult").arg(invocation);
    }
  }

}
