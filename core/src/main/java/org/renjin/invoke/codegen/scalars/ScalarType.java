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
package org.renjin.invoke.codegen.scalars;

import com.sun.codemodel.*;
import org.renjin.invoke.annotations.DownCastComplex;
import org.renjin.invoke.model.JvmMethod.Argument;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.Vector;


public abstract class ScalarType {

  public abstract Class getScalarType();

  public abstract String getConversionMethod();

  public abstract String getAccessorMethod();

  public abstract Class getVectorType();

  public abstract Class<? extends Vector.Builder<?>> getBuilderClass();
  
  public abstract Class getBuilderArrayElementClass();

  public JExpression toBuildArrayElementType(JExpression resultValue) {
    return resultValue;
  }
  
  public abstract Class getArrayVectorClass();
  
  public JExpression testExpr(JCodeModel codeModel, JVar sexpVariable, Argument formal) {
    JClass vectorClass = codeModel.ref(Vector.class);
    JExpression vectorType =  codeModel.ref(getVectorType()).staticRef("VECTOR_TYPE");
    JExpression condition = sexpVariable._instanceof(vectorClass)
        .cand(vectorType.invoke("isWiderThanOrEqualTo").arg(JExpr.cast(vectorClass, sexpVariable)));

    if(formal.isAnnotatedWith(DownCastComplex.class)) {
      condition = condition.cor(sexpVariable._instanceof(codeModel.ref(ComplexVector.class)));
    }

    return condition;
  }

  public Vector.Type getVectorTypeInstance() {
    try {
      return (Vector.Type) getVectorType().getField("VECTOR_TYPE").get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public abstract JExpression naLiteral(JCodeModel codeModel);
  
  public JExpression testNaExpr(JCodeModel codeModel, JVar scalarVariable) {
    JExpression testNA = codeModel.ref(getVectorType()).staticInvoke("isNA").arg(scalarVariable);
    return testNA;
  }

  public Class<?> getElementStorageType() {
    return getScalarType();
  }

  public JExpression fromElementStorageType(JExpression expression) {

    return expression;
  }
  
}
