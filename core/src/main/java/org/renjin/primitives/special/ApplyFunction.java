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
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.MatchedArguments;
import org.renjin.invoke.annotations.CompilerSpecialization;
import org.renjin.primitives.Types;
import org.renjin.primitives.combine.Combine;
import org.renjin.sexp.*;

public abstract class ApplyFunction extends SpecialFunction {

  public ApplyFunction(String name) {
    super(name);
  }

  protected static Vector simplifyToArray(ListVector list, boolean higher) {

    if(list.length() == 0) {
      return list;
    }

    int commonLength = commonLength(list);
    if(commonLength == -1) {
      return list;
    }

    if(commonLength == 1) {
      return (Vector) Combine.unlist(list, false, true);

    } else if(commonLength > 1) {
      throw new UnsupportedOperationException("TODO");
    } else {
      return list;
    }
  }

  private static int commonLength(ListVector list) {
    int i = 0;
    int length = list.getElementAsSEXP(i).length();
    for(i = 1; i < list.length(); ++i) {
      int elementLength = list.getElementAsSEXP(i).length();
      if (elementLength != length) {
        return -1;
      }
    }
    return length;
  }

  protected final PairList promiseExtraArguments(Environment rho, MatchedArguments matched) {
    PairList.Builder extra = new PairList.Builder();
    for (int i = 0; i < matched.getActualCount(); i++) {
      if(matched.isExtraArgument(i)) {
        extra.add(matched.getActualTag(i), Promise.repromise(rho, matched.getActualValue(i)));
      }
    }
    return extra.build();
  }

  protected ListVector applyList(Context context, Environment rho, SEXP vector, SEXP function, PairList extraArguments) {

    if(!Types.isVector(vector, "any") || Types.isObject(vector)) {
      FunctionCall asListCall = FunctionCall.newCall(Symbol.get("as.list"), Promise.repromise(vector));
      vector = context.evaluate(asListCall, rho);
    }

    ListVector.Builder builder = ListVector.newBuilder();
    builder.setAttribute(Symbols.NAMES, vector.getAttributes().getNamesOrNull());

    for(int i=0;i!=vector.length();++i) {
      // For historical reasons, the calls created by lapply are unevaluated, and code has
      // been written (e.g. bquote) that relies on this.
      FunctionCall getElementCall = FunctionCall.newCall(Symbol.get("[["), vector, new IntArrayVector(i+1));
      FunctionCall applyFunctionCall = new FunctionCall(function, new PairList.Node(getElementCall, extraArguments));
      builder.add( context.evaluate(applyFunctionCall, rho) );
    }
    return builder.build();
  }

  protected final Function matchFunction(Context context, Environment rho, SEXP functionArgument) {
    functionArgument = functionArgument.force(context);
    SEXP evaluatedArgument = context.evaluate(functionArgument, rho).force(context);

    if (evaluatedArgument instanceof Function) {
      return (Function) evaluatedArgument;
    }

    Symbol name;
    if (evaluatedArgument instanceof Symbol) {
      name = (Symbol) evaluatedArgument;

    } else if (evaluatedArgument instanceof StringVector && evaluatedArgument.length() == 1) {
      name = Symbol.get(((StringVector) evaluatedArgument).getElementAsString(0));

    } else if(functionArgument instanceof Symbol) {

      // Fallback to the unevaluated name if the evaluated argument is not a function or a character
      // string

      name = (Symbol) functionArgument;

    } else {
      throw new EvalException("'%s' is not a function, character or symbol", evaluatedArgument.toString());
    }

    Function function = rho.findFunction(context, name);
    if (function == null) {
      throw new EvalException("Function '" + name + " not found");
    }

    return function;
  }

  protected SEXP tryCompileAndEval(Context context, Environment rho, FunctionCall call,
                                   Vector vector,
                                   SEXP functionArgument,
                                   SEXP function, boolean simplify) {

    String applyfn = (simplify ? "S" : "L") + "APPLY";
    System.out.println(applyfn + " " + vector.length() + " @" + Integer.toHexString(System.identityHashCode(call)));

    if(call.cache == Failed.COMPILATION) {
      return null;
    }

    if(call.cache instanceof CachedApplyCall) {
      CachedApplyCall cached = (CachedApplyCall) call.cache;
      if(cached.assumptionsStillMet(context, rho, vector)) {
        System.out.println("Reusing cached " + applyfn + "()");
        return cached.getCompiledCall().apply(context, rho, vector);
      } else {
        System.out.println("Invalidated " + applyfn + "(), recompiling...");
      }
    }

    if (!(function instanceof Closure)) {
      return null;
    }

    Closure closure = (Closure) function;

    CompiledApplyCall compiledCall;

    try {

      CachedApplyCall compiled = SexpCompiler.compileApplyCall(context, rho, vector, closure, simplify);

      // Cache for subsequent evaluations...
      call.cache = compiled;
      compiledCall = compiled.getCompiledCall();

    } catch (NotCompilableException e) {
      if (ForFunction.FAIL_ON_COMPILATION_ERROR) {
        throw new AssertionError(applyfn + "() compilation failed: " + e.toString(context));
      }
      System.out.println("Could not compile " + applyfn + "() because: " + e.toString(context));

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
      System.out.println("Running compiled " + applyfn + "()");
      return compiledCall.apply(context, rho, vector);
    }

    return null;
  }

  private enum Failed {
    COMPILATION;
  }

  @CompilerSpecialization
  public static Vector build(SEXP vector, SEXP[] array, boolean simplify, boolean useNames) {

    ListVector list = ListVector.unsafe(array, buildAttributes(vector, useNames));

    if(simplify) {
      return simplifyToArray(list, false);
    } else {
      return list;
    }
  }

  public static Vector build(SEXP vector, double[] array, boolean useNames) {
    return DoubleArrayVector.unsafe(array, buildAttributes(vector, useNames));
  }

  private static AttributeMap buildAttributes(SEXP vector, boolean useNames) {
    AtomicVector names = vector.getAttributes().getNamesOrNull();
    if(names == Null.INSTANCE && vector instanceof StringVector && useNames) {
      names = (AtomicVector) vector;
    }
    AttributeMap attributes;
    if(names == Null.INSTANCE) {
      attributes = AttributeMap.EMPTY;
    } else {
      attributes = AttributeMap.builder().setNames(names).build();
    }
    return attributes;
  }

}
