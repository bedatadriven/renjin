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

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector.Builder;

public class SexpType extends ScalarType {

  @Override
  public Class getScalarType() {
    return SEXP.class;
  }

  @Override
  public String getConversionMethod() {
    throw new UnsupportedOperationException();  
  }

  @Override
  public String getAccessorMethod() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class getVectorType() {
    return ListVector.class;
  }

  @Override
  public Class<? extends Builder<?>> getBuilderClass() {
    return ListVector.Builder.class;
  }

  @Override
  public Class getBuilderArrayElementClass() {
    return SEXP.class;
  }

  @Override
  public Class getArrayVectorClass() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JExpression naLiteral(JCodeModel codeModel) {
    throw new UnsupportedOperationException();
  }

}
