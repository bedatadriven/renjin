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
package org.renjin.compiler;

import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.cfg.LiveSet;
import org.renjin.compiler.cfg.UseDefMap;
import org.renjin.compiler.codegen.ByteCodeEmitter;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

/**
 * Created by alex on 28-9-16.
 */
public class Compiler {

  public static CompiledBody tryCompile(Context context, Environment rho, SEXP expression) {
    try {

      RuntimeState runtimeState = new RuntimeState(context, rho);
      IRBodyBuilder builder = new IRBodyBuilder(runtimeState);
      IRBody body = builder.build(expression);

      ControlFlowGraph cfg = new ControlFlowGraph(body);


      DominanceTree dTree = new DominanceTree(cfg);
      SsaTransformer ssaTransformer = new SsaTransformer(cfg, dTree);
      ssaTransformer.transform();

      System.out.println(cfg);

      UseDefMap useDefMap = new UseDefMap(cfg);
      TypeSolver types = new TypeSolver(cfg, useDefMap);
      types.execute();

      LiveSet liveSet = new LiveSet(dTree, useDefMap);

      types.dumpBounds();

      types.verifyFunctionAssumptions(runtimeState);

      ssaTransformer.removePhiFunctions(types);

      System.out.println(cfg);

      ByteCodeEmitter emitter = new ByteCodeEmitter(cfg, liveSet, types);
      return emitter.compile().newInstance();

    } catch (NotCompilableException e) {
      System.out.println(e.toString());
      context.warn("Could not compile loop: " + e.toString(context));
      return null;

    } catch (InvalidSyntaxException e) {
      e.printStackTrace();
      throw new EvalException(e.getMessage());

    } catch (Exception e) {
      throw new EvalException("Exception compiling loop: " + e.getMessage(), e);
    }
  }

  public static boolean tryCompileAndRun(Context context, Environment rho, SEXP expression) {

    CompiledBody body = tryCompile(context, rho, expression);
    if(body == null) {
      return false;
    }

    body.evaluate(context, rho);

    return true;
  }
}
