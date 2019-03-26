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
package org.renjin.primitives.special;

import org.renjin.compiler.CachedBody;
import org.renjin.compiler.CompiledBody;
import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.SexpCompiler;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

public class WhileFunction extends SpecialFunction {

  public WhileFunction() {
    super("while");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    SEXP condition = args.getElementAsSEXP(0);
    SEXP statement = args.getElementAsSEXP(1);
    
    int iterationCount = 0;
    boolean compilationFailed = false;

    while(asLogicalNoNA(context, call, context.evaluate(condition, rho))) {

      try {
        iterationCount ++;

        if(ForFunction.COMPILE_LOOPS && iterationCount > 50 && !compilationFailed) {
          if(tryCompileAndRun(context, rho, call)) {
            break;
          } else {
            compilationFailed = true;
          }
        }
        
        context.evaluate( statement, rho);
        
      } catch(BreakException e) {
        break;
      } catch(NextException e) {
        // next loop iteration
      }
    }
    context.setInvisibleFlag();
    return Null.INSTANCE;
  }


  public static boolean tryCompileAndRun(Context context, Environment rho, FunctionCall call) {

    CompiledBody compiledBody = null;

    if(call.cache instanceof CachedBody) {
      CachedBody cachedLoopBody = (CachedBody) call.cache;
      if(cachedLoopBody.assumptionsStillMet(context, rho)) {
        compiledBody = cachedLoopBody.getCompiledBody();
      }
    }

    if(compiledBody == null) {
      try {

        CachedBody compiled = SexpCompiler.compileSexp(context, rho, call);

        // Cache for subsequent evaluations...
        call.cache = compiled;
        compiledBody = compiled.getCompiledBody();

      } catch (NotCompilableException e) {
        if (ForFunction.FAIL_ON_COMPILATION_ERROR) {
          throw new AssertionError("Loop compilation failed: " + e.toString(context));
        }
        context.warn("Could not compile while loop because: " + e.toString(context));
        return false;

      } catch (InvalidSyntaxException e) {
        throw new EvalException(e.getMessage());

      } catch (Exception e) {
        throw new EvalException("Exception compiling loop: " + e.getMessage(), e);
      }
    }

    compiledBody.evaluate(context, rho);
    return true;
  }
}
