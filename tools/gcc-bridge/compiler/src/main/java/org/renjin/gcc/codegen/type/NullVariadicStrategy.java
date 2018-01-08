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
package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.tree.AnnotationNode;

import java.util.Collections;
import java.util.List;


public class NullVariadicStrategy implements VariadicStrategy {
  @Override
  public List<Type> getParameterTypes() {
    return Collections.emptyList();
  }

  @Override
  public List<AnnotationNode> getParameterAnnotations() {
    return Collections.emptyList();
  }

  @Override
  public List<JExpr> marshallVarArgs(MethodGenerator mv, ExprFactory exprFactory, List<GimpleExpr> extraArgs) {
    return Collections.emptyList();
  }


}
