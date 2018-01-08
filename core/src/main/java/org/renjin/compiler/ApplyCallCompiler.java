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
package org.renjin.compiler;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.builtins.ArgumentBounds;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.cfg.InlinedFunction;
import org.renjin.compiler.cfg.UseDefMap;
import org.renjin.compiler.codegen.ApplyCallWriter;
import org.renjin.compiler.codegen.ByteCodeEmitter;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.expressions.ReadParam;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Compiles calls to lapply, vapply, sapply, etc to JVM bytecode
 */
public class ApplyCallCompiler {

  private final RuntimeState runtimeState;
  private final Closure closure;
  private final Vector vector;
  private InlinedFunction inlinedFunction;
  private ValueBounds functionBounds;
  private boolean pure;

  public ApplyCallCompiler(Context context, Environment rho, Closure closure, Vector vector) {
    this.closure = closure;
    this.vector = vector;
    this.runtimeState = new RuntimeState(context, rho);
  }

  public void tryCompile() {

    // The IRBody builder needs to know which arguments will be provided in order to support
    // default argument values and other chicanery.

    // For right now, we only provide the first argument to the apply function

    Set<Symbol> suppliedFormals = Collections.singleton(findElementFormal(closure));
    List<ArgumentBounds> arguments = Lists.newArrayList(new ArgumentBounds(computeElementBounds()));

    // Now try to compile the function and resolve types
    inlinedFunction = new InlinedFunction(runtimeState, closure, suppliedFormals);
    functionBounds = inlinedFunction.updateBounds(arguments);
    pure = inlinedFunction.isPure();
    System.out.println("F = " + functionBounds);
  }

  private ValueBounds computeElementBounds() {
    return ValueBounds.of(vector).getElementBounds();
  }

  private Symbol findElementFormal(Closure closure) {
    PairList formals = closure.getFormals();
    if(formals == Null.INSTANCE) {
      throw new EvalException("unused argument X[[i]]");
    }
    return formals.getTag();
  }

  public ValueBounds getFunctionBounds() {
    return functionBounds;
  }

  public boolean isPure() {
    return pure;
  }

  public Class<?> compile() {
    ApplyCallWriter writer = new ApplyCallWriter(inlinedFunction, closure.getFormals().getTag(), computeElementBounds(), functionBounds);
    return writer.build();
  }

}
