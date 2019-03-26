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
package org.renjin.invoke.codegen.scalars;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalVector;

public class LogicalType extends ScalarType {


  @Override
  public Class getScalarType() {
    return Logical.class;
  }

  @Override
  public String getConversionMethod() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getAccessorMethod() {
    return "getElementAsLogical";
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
  public JExpression naLiteral(JCodeModel codeModel) {
    return codeModel.ref(IntVector.class).staticRef("NA");
  }


}
