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
package org.renjin.gnur;

import org.renjin.gcc.TreeLogger;
import org.renjin.gcc.analysis.ControlFlowGraph;
import org.renjin.gcc.analysis.DataFlowAnalysis;
import org.renjin.gcc.analysis.FunctionBodyTransformer;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.repackaged.guava.collect.HashMultimap;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Multimap;
import org.renjin.sexp.SexpType;

import java.util.Collection;

/**
 * Rewrites SET_TYPEOF calls.
 *
 * <p>A common idiom in C code for R packages is to create a pair list using {@code allocList} and
 * then later call {@code SET_TYPEOF} to convert it to a language (function call) object. Since
 * we can't support that with Renjin currently, so we want to change the allocating call to our own
 * {@code Rf_allocLang}</p>
 */
public class SetTypeRewriter implements FunctionBodyTransformer {
  @Override
  public boolean transform(TreeLogger logger, GimpleCompilationUnit unit, GimpleFunction fn) {

    // If there are no calls to SET_TYPEOF in this function, we have nothing to do.
    if(!hasSetTypeOfCall(fn)) {
      return false;
    }

    // Otherwise we need to trace back the argument of SET_TYPEOF to the call site where it
    // was created.
    ControlFlowGraph cfg = new ControlFlowGraph(fn);
    SexpFlowFunction flowFunction = new SexpFlowFunction(unit, fn);
    DataFlowAnalysis<Multimap<Long, GimpleCall>> flowAnalysis = new DataFlowAnalysis<>(cfg, flowFunction);
    flowAnalysis.dump();

    // Now identify the target types of the allocations
    Multimap<GimpleCall, Integer> typeMap = HashMultimap.create();
    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      for (GimpleStatement statement : basicBlock.getStatements()) {
        if(isSetTypeCall(statement)) {
          GimpleCall setTypeCall = (GimpleCall) statement;
          if(setTypeCall.getOperand(0) instanceof GimpleVariableRef) {
            GimpleVariableRef target = (GimpleVariableRef) setTypeCall.getOperand(0);
            Collection<GimpleCall> allocSites = flowAnalysis.getState(basicBlock, statement).get(target.getId());
            int targetType = getTargetType(statement);

            for (GimpleCall allocSite : allocSites) {
              typeMap.put(allocSite, targetType);
            }
          }
        }
      }
    }

    // Now update the allocation sites

    boolean changed = false;

    for (GimpleCall allocSite : typeMap.keySet()) {
      Collection<Integer> types = typeMap.get(allocSite);
      if(types.size() == 1) {
        int targetType = Iterables.getOnlyElement(types);
        if(allocSite.isFunctionNamed("Rf_allocList") && targetType == SexpType.LANGSXP) {
          allocSite.setFunction(new GimpleAddressOf(new GimpleFunctionRef("Rf_allocLang")));
          changed = true;
        }
      }
    }

    return changed;
  }

  private boolean hasSetTypeOfCall(GimpleFunction fn) {
    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      for (GimpleStatement statement : basicBlock.getStatements()) {
        if (isSetTypeCall(statement)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isSetTypeCall(GimpleStatement statement) {
    if(statement instanceof GimpleCall) {
      GimpleCall call = (GimpleCall) statement;
      if(call.isFunctionNamed("SET_TYPEOF")) {
        return true;
      }
    }
    return false;
  }

  private int getTargetType(GimpleStatement statement) {
    GimpleCall call = (GimpleCall) statement;
    if(call.getOperands().size() == 2) {
      GimpleExpr typeOperand = call.getOperand(1);
      if(typeOperand instanceof GimpleIntegerConstant) {
        GimpleIntegerConstant typeConstant = (GimpleIntegerConstant) typeOperand;
        return typeConstant.getNumberValue().intValue();
      }
    }
    return -1;
  }
}
