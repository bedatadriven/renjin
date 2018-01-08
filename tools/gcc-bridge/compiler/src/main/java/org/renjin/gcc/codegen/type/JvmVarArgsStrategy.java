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
package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.primitive.PrimitiveExpr;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.tree.AnnotationNode;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.Collections;
import java.util.List;

import static org.renjin.gcc.codegen.expr.Expressions.box;

/**
 * Handles extra arguments passed to a JVM method with a "varargs" argument of type {@code Object[]}.
 *
 * <p>This strategy is only here to support the existing implementation of {@link org.renjin.gcc.runtime.Stdlib.printf()} and
 * can be removed once we have a better implementation.</p>
 */
public class JvmVarArgsStrategy implements VariadicStrategy {
  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(Object[].class));
  }

  @Override
  public List<JExpr> marshallVarArgs(MethodGenerator mv, ExprFactory exprFactory, List<GimpleExpr> extraArgs) {
    List<JExpr> varArgValues = Lists.newArrayList();
    for (GimpleExpr arg : extraArgs) {
      GExpr varArgExpr = exprFactory.findGenerator(arg);
      varArgValues.add(wrapVarArg(varArgExpr));
    }
    return Collections.singletonList(Expressions.newArray(Type.getType(Object.class), varArgValues));
  }

  @Override
  public List<AnnotationNode> getParameterAnnotations() {
    return Collections.singletonList(null);
  }

  private JExpr wrapVarArg(GExpr varArgExpr) {
    // TODO: generalize
    // This is quite specific to printf()
    if(varArgExpr instanceof PrimitiveExpr) {
      return box(((PrimitiveExpr) varArgExpr).jexpr());

    } else if(varArgExpr instanceof GSimpleExpr) {
      return ((GSimpleExpr) varArgExpr).jexpr();

    } else if(varArgExpr instanceof PtrExpr) {
      return varArgExpr.toVPtrExpr().getRef();

    } else {
      throw new UnsupportedOperationException("varArgExpr: " + varArgExpr);
    }
  }

}
