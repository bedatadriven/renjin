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
package org.renjin.gnur;

import org.renjin.gcc.codegen.GlobalVarTransformer;
import org.renjin.gcc.codegen.expr.ConstantValue;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimplePrimitiveConstant;
import org.renjin.repackaged.asm.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Identifies global variables with read/write access and moves them
 * to a session-scoped class.
 */
public class GlobalVarRewriter implements GlobalVarTransformer {

  private final ContextVarAllocator allocator;

  private List<GimpleVarDecl> globalVariables = new ArrayList<>();

  public GlobalVarRewriter(Type contextClass) {
    allocator = new ContextVarAllocator(contextClass);
  }

  @Override
  public boolean acceptGlobalVar(GimpleVarDecl decl) {

    // Do not include C++ run-time type information in the per-thread context.
    // It does not change after initialization.

    if(decl.getMangledName().equals("_ZTVN10__cxxabiv120__si_class_type_infoE")) {
      return false;
    }

    return true;
  }

  @Override
  public boolean acceptLocalStaticVar(GimpleVarDecl decl) {
    return true;
  }

  @Override
  public GExpr generator(TypeOracle typeOracle, GimpleCompilationUnit unit, GimpleVarDecl decl) {

    globalVariables.add(decl);

    return typeOracle.forType(decl.getType()).variable(decl, allocator.forUnit(unit));
  }

  @Override
  public GExpr generator(TypeOracle typeOracle, GimpleFunction function, GimpleVarDecl decl) {
    if(!hasSafeInitialValue(decl)) {
      throw new UnsupportedOperationException(String.format("TODO: %s in %s in %s has initial value %s",
          decl.getName(),
          function.getName(),
          function.getUnit().getSourceName(),
          decl.getValue().toString()));
    }
    globalVariables.add(decl);
    return typeOracle.forType(decl.getType()).variable(decl, allocator.forFunction(function));
  }

  private boolean hasSafeInitialValue(GimpleVarDecl decl) {
    return decl.getValue() == null || decl.getValue() instanceof GimpleConstant;
  }

  public List<ContextField> getContextFields() {
    return allocator.getContextFields();
  }

  public List<GimpleVarDecl> getGlobalVariables() {
    return globalVariables;
  }
}
