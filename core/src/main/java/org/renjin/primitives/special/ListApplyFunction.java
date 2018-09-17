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

import org.renjin.compiler.CachedApplyCall;
import org.renjin.compiler.CompiledApplyCall;
import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.SexpCompiler;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.functions.ListApplyTranslator;
import org.renjin.eval.ArgumentMatcher;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.MatchedArguments;
import org.renjin.sexp.*;

public class ListApplyFunction extends ApplyFunction {

  public static final ArgumentMatcher MATCHER = new ArgumentMatcher("X", "FUN", "...");

  private enum Failed {
    COMPILATION;
  }

  public ListApplyFunction() {
    super("lapply");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    MatchedArguments matched = MATCHER.expandAndMatch(context, rho, args);
    SEXP vector = context.evaluate(matched.getActualForFormal(0), rho);
    SEXP functionArgument = matched.getActualForFormal(1);

    if(vector.length() >= 120 && vector instanceof Vector)  {
      SEXP result = tryCompileAndEval(context, rho, call, (Vector) vector, functionArgument);
      if(result != null) {
        return result;
      }
    }

    Function function = matchFunction(context, rho, functionArgument);

    PairList extraArguments = promiseExtraArguments(rho, matched);

    return applyList(context, rho, vector, function, extraArguments)
        .setAttribute(Symbols.NAMES, vector.getAttributes().getNamesOrNull())
        .build();
  }

  private SEXP tryCompileAndEval(Context context, Environment rho, FunctionCall call, Vector vector, SEXP functionArgument) {


    if (!ListApplyTranslator.isClosureDefinition(functionArgument)) {
      return null;
    }
    System.out.println("LAPPLY " + vector.length() + " @" + Integer.toHexString(System.identityHashCode(call)));


    if(call.cache == Failed.COMPILATION) {
      return null;
    }

    if(call.cache instanceof CachedApplyCall) {
      CachedApplyCall cached = (CachedApplyCall) call.cache;
      if(cached.assumptionsStillMet(context, rho, vector)) {
        System.out.println("Reusing cached lapply()");
        return cached.getCompiledCall().apply(context, rho, vector);
      } else {
        System.out.println("Invalidated lapply(), recompiling...");
      }
    }

    Closure closure = ClosureFunction.apply(rho, ((FunctionCall) functionArgument).getArguments());
    CompiledApplyCall compiledCall;

    try {

      CachedApplyCall compiled = SexpCompiler.compileApplyCall(context, rho, vector, closure);

      // Cache for subsequent evaluations...
      call.cache = compiled;
      compiledCall = compiled.getCompiledCall();

    } catch (NotCompilableException e) {
      if (ForFunction.FAIL_ON_COMPILATION_ERROR) {
        throw new AssertionError("lapply() compilation failed: " + e.toString(context));
      }
      System.out.println("Could not compile lapply() because: " + e.toString(context));
      if(call.cache == null) {
        call.cache = Failed.COMPILATION;
      }
      return null;

    } catch (InvalidSyntaxException e) {
      throw new EvalException(e.getMessage());

    } catch (Exception e) {
      e.printStackTrace();
      throw new EvalException("Exception compiling loop: " + e.getMessage(), e);
    }

    if(compiledCall != null) {
      System.out.println("Running compiled lapply()");
      return compiledCall.apply(context, rho, vector);
    }

    return null;
  }
}
