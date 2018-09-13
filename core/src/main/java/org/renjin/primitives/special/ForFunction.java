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
package org.renjin.primitives.special;

import org.renjin.compiler.CachedLoopBody;
import org.renjin.compiler.CompiledLoopBody;
import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.SexpCompiler;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Profiler;
import org.renjin.sexp.*;


public class ForFunction extends SpecialFunction {

  public static boolean COMPILE_LOOPS = Boolean.getBoolean("renjin.compile.loops");
  public static boolean COMPILE_LOOPS_VERBOSE = Boolean.getBoolean("renjin.compile.loops.verbose");

  public static boolean FAIL_ON_COMPILATION_ERROR = false;
  
  private static final int COMPILE_THRESHOLD = 200;
  private static final int WARMUP_ITERATIONS = 0;

  public ForFunction() {
    super("for");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList _args_unused) {

    
    PairList args = call.getArguments();
    Symbol symbol = args.getElementAsSEXP(0);
    SEXP elementsExp = context.evaluate(args.getElementAsSEXP(1), rho);
    if(!(elementsExp instanceof Vector)) {
      throw new EvalException("invalid for() loop sequence");
    }
    Vector elements = (Vector) elementsExp;
    SEXP statement = args.getElementAsSEXP(2);

    boolean profiling = Profiler.ENABLED && elements.length() > COMPILE_THRESHOLD;
    if(profiling) {
      Profiler.loopStart(call, elements);
    }

    int i = 0;
    
    try {
      // Interpret the loop
      boolean compilationFailed = false;
      for (i = 0; i != elements.length(); ++i) {
        try {

          if (COMPILE_LOOPS && i >= WARMUP_ITERATIONS && elements.length() > COMPILE_THRESHOLD &&
              !compilationFailed) {

            if (tryCompileAndRun(context, rho, call, elements, i)) {
              break;
            } else {
              compilationFailed = true;
            }
          }

          rho.setVariable(context, symbol, elements.getElementAsSEXP(i));
          context.evaluate(statement, rho);
        } catch (BreakException e) {
          break;
        } catch (NextException e) {
          // next iteration
        }
      }
    } finally {
      if(profiling) {
        Profiler.loopEnd(i);
      }
    }

    context.setInvisibleFlag();
    return Null.INSTANCE;
  }

  public boolean tryCompileAndRun(Context context, Environment rho, FunctionCall call, Vector sequence, int i) {

    CompiledLoopBody compiledBody = null;

    if(call.cache instanceof CachedLoopBody) {
      CachedLoopBody cachedLoopBody = (CachedLoopBody) call.cache;
      if(cachedLoopBody.assumptionsStillMet(context, rho, sequence)) {
        System.out.println("Reusing cached loop body...");
        compiledBody = cachedLoopBody.getCompiledBody();
      } else {
        System.out.println("Discarding cached loop body...");
      }
    }

    if(compiledBody == null) {
      try {

        CachedLoopBody compiled = SexpCompiler.compileForLoop(context, rho, call, sequence);

        // Cache for subsequent evaluations...
        call.cache = compiled;
        compiledBody = compiled.getCompiledBody();

      } catch (NotCompilableException e) {
        if (ForFunction.FAIL_ON_COMPILATION_ERROR) {
          throw new AssertionError("Loop compilation failed: " + e.toString(context));
        }
        context.warn("Could not compile loop because: " + e.toString(context));
        return false;

      } catch (InvalidSyntaxException e) {
        throw new EvalException(e.getMessage());

      } catch (Exception e) {
        e.printStackTrace();
        throw new EvalException("Exception compiling loop: " + e.getMessage(), e);
      }
    }

    compiledBody.run(context, rho, sequence, i);

    System.out.println("Compiled loop body executed.");

    return true;
  }

}
