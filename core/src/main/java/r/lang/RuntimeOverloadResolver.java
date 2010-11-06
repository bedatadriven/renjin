/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.lang;

import r.lang.exception.EvalException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RuntimeOverloadResolver {

  public static final RuntimeOverloadResolver INSTANCE = new RuntimeOverloadResolver();

  private List<CallStrategy> strategies;

  private RuntimeOverloadResolver() {
    strategies = new ArrayList<CallStrategy>();
    strategies.add(new FixedArity());
    strategies.add(new RealUnary());
    strategies.add(new RealBinary());
    strategies.add(new VarArgs());
  }

  public EvalResult invoke(EnvExp rho, LangExp call, List<Method> overloads) {

    // first check for a method which can handle the call in its entirety
    if(overloads.size() == 1 && isLangMethod(overloads.get(0))) {
      return invokeAndWrap(overloads.get(0), rho, call);
    }

    SEXP args[] = evaluateArguments(rho, call.getArguments());

    for(Method method : overloads) {
      for(CallStrategy strategy : strategies) {
        if(strategy.accept(method, args)) {
          return strategy.apply(method, args);
        }
      }
    }
    throw new EvalException("No suitable overload for provided arguments");
  }

  public static boolean isLangMethod(Method method) {
    Class<?>[] classes = method.getParameterTypes();
    return classes.length == 2 &&
           classes[0] == EnvExp.class &&
           classes[1] == LangExp.class;
  }

  private SEXP[] evaluateArguments(EnvExp rho, PairList arguments) {
    SEXP evaluatedArguments[] = new SEXP[arguments.length()];
    int i=0;
    for(SEXP arg : arguments) {
      evaluatedArguments[i++] = arg.evaluate(rho).getExpression();
    }
    return evaluatedArguments;
  }


  private interface CallStrategy {
    boolean accept(Method method, SEXP arguments[]);
    EvalResult apply(Method method, SEXP arguments[]);
  }

  /**
   * Evaluates and passes a fixed number of {@code SEXP}s
   * to the given method.
   */
  private class FixedArity implements CallStrategy {

    @Override
    public boolean accept(Method method, SEXP providedArgs[]) {
      Class<?>[] expected = method.getParameterTypes();
      if(providedArgs.length != expected.length) {
        return false;
      }
      for(int i=0; i!=expected.length; ++i) {
        if(!expected[i].isAssignableFrom(providedArgs[i].getClass())) {
          return false;
        }
      }
      return true;
    }

    @Override
    public EvalResult apply(Method method, SEXP arguments[]) {
      return invokeAndWrap(method, arguments);
    }
  }

  private class VarArgs implements CallStrategy {

    @Override
    public boolean accept(Method method, SEXP[] arguments) {
      return method.isVarArgs() &&
         method.getParameterTypes().length == 1 &&
         method.getParameterTypes()[0] == Array.class;
    }

    @Override
    public EvalResult apply(Method method, SEXP[] arguments) {
      return invokeAndWrap(method, arguments);
    }
  }

  /**
   * Applies a function(double x) to a RealExp
   */
  private class RealUnary implements CallStrategy {
    @Override
    public boolean accept(Method method, SEXP arguments[]) {
      return hasArgs(method, Double.TYPE) && method.getReturnType() == Double.TYPE &&
          arguments.length == 1 &&
          arguments[0] instanceof RealExp;
    }

    @Override
    public EvalResult apply(Method method, SEXP arguments[]) {
      RealExp domain = (RealExp) arguments[0];
      double[] range = new double[domain.length()];

      for (int i = 0; i < range.length; i++) {
        if (Double.isNaN(domain.get(i))) {
          range[i] = domain.get(i);
        } else {
          range[i] = (Double)invoke(method, domain.get(i));
        }
      }

      return new EvalResult( new RealExp( range ) );
    }
  }

  private class RealBinary implements CallStrategy {
    @Override
    public boolean accept(Method method, SEXP arguments[]) {
      return hasArgs(method, Double.TYPE, Double.TYPE) && method.getReturnType() == Double.TYPE &&
          arguments.length == 2 &&
          arguments[0] instanceof RealExp &&
          arguments[1] instanceof RealExp;
    }

    @Override
    public EvalResult apply(Method method, SEXP arguments[]) {
      RealExp x = (RealExp) arguments[0];
      RealExp y = (RealExp) arguments[1];
      int xLen = x.length();
      int yLen = y.length();
      int maxLen = Math.max(xLen, yLen);
      int minLen = Math.min(xLen, yLen);
      double result[] = new double[maxLen];

      if( maxLen % minLen != 0) {
        throw new EvalException("longer object length is not a multiple of shorter object length");
      }

      for(int i=0; i!=result.length; i++) {

        double xi = x.get( i % xLen );
        double yi = y.get( i % yLen );

        result[i] = (Double)invoke(method, xi, yi);
      }

      return new EvalResult( new RealExp(result ) );
    }
  }

  private static boolean hasArgs(Method method, Class... expectedArguments) {
    return Arrays.equals(method.getParameterTypes(), expectedArguments);
  }


  private static EvalResult invokeAndWrap(Method overload, Object... arguments) {
    return wrap(overload.getReturnType(), invoke(overload, arguments));
  }

  private static <X> X invoke(Method overload, Object... arguments) {
    try {
      return (X) overload.invoke(null, arguments);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Access exception while invoking method:\n" + overload.toString(), e);
    } catch (InvocationTargetException e) {
      if(e.getCause() instanceof RuntimeException) {
        // Rethrow Runtime Exceptions
        throw (RuntimeException)e.getCause();
      } else {
        // wrap checked exceptions
        throw new RuntimeException("Exception while invoking method from R:\n" + overload.toString(), e);
      }
    }
  }

  private static EvalResult wrap(Class declaredReturnType, Object result) {
    if(declaredReturnType == Void.TYPE) {
      return EvalResult.NON_PRINTING_NULL;

    } else if(result instanceof EvalResult) {
      return (EvalResult) result;

    } else if(result instanceof SEXP) {
      return new EvalResult((SEXP) result);

    } else if(result instanceof Long) {
      return new EvalResult( new RealExp(((Long)result).doubleValue()) );
      
    } else {
      throw new EvalException("Java function returned incovertable type: " + result.getClass().getName());
    }
  }
}
