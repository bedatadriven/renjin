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
package org.renjin.gcc.analysis;

import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.primitive.NumericIntExpr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveTypeStrategy;
import org.renjin.gcc.codegen.type.primitive.PtrCarryingExpr;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

/**
 * Provides insight into all things related to a function, its parameters, and variables.
 */
public class FunctionOracle {

  private final TypeOracle typeOracle;

  private final VarSet pointerCarryingIntegerVariables;

  public FunctionOracle(TypeOracle typeOracle, GimpleFunction function) {
    this.typeOracle = typeOracle;

    ControlFlowGraph cfg = new ControlFlowGraph(function);

    // Find pointer-carrying integers
    DataFlowAnalysis<VarSet> pointerCarrierAnalysis = new DataFlowAnalysis<>(cfg, new PtrCarrierFlowFunction());
    pointerCarryingIntegerVariables = VarSet.unionAll(pointerCarrierAnalysis.getExitStates());
  }


  public GExpr variable(GimpleVarDecl varDecl, VarAllocator allocator) {
    if(pointerCarryingIntegerVariables.contains(varDecl)) {
      PrimitiveTypeStrategy primitiveTypeStrategy = new PrimitiveTypeStrategy((GimplePrimitiveType) varDecl.getType());
      NumericIntExpr integerExpr = (NumericIntExpr) primitiveTypeStrategy.variable(varDecl, allocator);
      JExpr pointerVariable = allocator.reserve(varDecl.getNameIfPresent("ptr"), Type.getType(Ptr.class));

      return new PtrCarryingExpr(integerExpr, pointerVariable);
    }

    return typeOracle.forType(varDecl.getType()).variable(varDecl, allocator);
  }
}
