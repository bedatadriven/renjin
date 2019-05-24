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
package org.renjin.gcc.codegen;

import org.renjin.gcc.ProvidedGlobalVar;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;

import java.util.Map;

public class ProvidedVarTransformer implements GlobalVarTransformer {

  private final TypeOracle typeOracle;
  private final Map<String, ProvidedGlobalVar> providedVariables;

  public ProvidedVarTransformer(TypeOracle typeOracle, Map<String, ProvidedGlobalVar> providedVariables) {
    this.typeOracle = typeOracle;
    this.providedVariables = providedVariables;
  }

  @Override
  public boolean acceptGlobalVar(GimpleVarDecl decl) {
    return decl.isPublic() && providedVariables.containsKey(decl.getName());
  }

  @Override
  public boolean acceptLocalStaticVar(GimpleVarDecl decl) {
    return false;
  }

  @Override
  public GExpr generator(TypeOracle typeOracle, GimpleCompilationUnit unit, GimpleVarDecl decl) {
    return providedVariables.get(decl.getName()).createExpr(decl, this.typeOracle);
  }

  @Override
  public GExpr generator(TypeOracle typeOracle, GimpleFunction function, GimpleVarDecl decl) {
    throw new UnsupportedOperationException();
  }
}
