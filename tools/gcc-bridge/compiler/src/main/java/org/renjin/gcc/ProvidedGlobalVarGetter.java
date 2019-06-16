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
package org.renjin.gcc;

import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

import java.lang.reflect.Method;

public class ProvidedGlobalVarGetter implements ProvidedGlobalVar {

  private Method getterMethod;

  public ProvidedGlobalVarGetter(Method getterMethod) {
    this.getterMethod = getterMethod;
  }

  @Override
  public GExpr createExpr(GimpleVarDecl decl, TypeOracle typeOracle) {

    JExpr jexpr = Expressions.staticMethodCall(getterMethod.getDeclaringClass(), getterMethod.getName(),
        Type.getMethodDescriptor(getterMethod));

    TypeStrategy strategy;
    if(typeOracle.getRecordTypes().isMappedToRecordType(getterMethod.getReturnType())) {
      strategy = typeOracle.getRecordTypes().getPointerStrategyFor(getterMethod.getReturnType());
    } else if (getterMethod.getReturnType().equals(Ptr.class)) {
      strategy = typeOracle.forPointerType(decl.getType()).pointerTo();
    } else {
      throw new UnsupportedOperationException("TODO: " + getterMethod.getReturnType());
    }

    return strategy.providedGlobalVariable(decl, jexpr, true);
  }
}
