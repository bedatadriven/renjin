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
package org.renjin.invoke.codegen.scalars;

import com.sun.codemodel.*;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Vector;

public class BooleanType extends ScalarType {

  @Override
  public Class getScalarType() {
    return Boolean.TYPE;
  }

  @Override
  public String getConversionMethod() {
    return "convertToBooleanPrimitive";
  }

  @Override
  public String getAccessorMethod() {
    return "getElementAsRawLogical";
  }

  @Override
  public Class getVectorType() {
    return LogicalVector.class;
  }

  @Override
  public Class<LogicalArrayVector.Builder> getBuilderClass() {
    return LogicalArrayVector.Builder.class;
  }

  @Override
  public Class getBuilderArrayElementClass() {
    return int.class;
  }

  @Override
  public Class getArrayVectorClass() {
    return LogicalArrayVector.class;
  }

  @Override
  public JExpression toBuildArrayElementType(JExpression resultValue) {
    return JOp.cond(resultValue, JExpr.lit(1), JExpr.lit(0));
  }

  @Override
  public JExpression testExpr(JCodeModel codeModel, JVar sexpVariable, JvmMethod.Argument formal) {
    return sexpVariable._instanceof(codeModel.ref(Vector.class));
  }

  @Override
  public JExpression naLiteral(JCodeModel codeModel) {
    return codeModel.ref(IntVector.class).staticRef("NA");
  }

  @Override
  public Class<?> getElementStorageType() {
    return int.class;
  }

  @Override
  public JExpression fromElementStorageType(JExpression expression) {
    return expression.ne(JExpr.lit(0));
  }
}
