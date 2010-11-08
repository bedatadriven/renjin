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
import r.parser.ParseUtil;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RuntimeOverloadResolver {

  public static final RuntimeOverloadResolver INSTANCE = new RuntimeOverloadResolver();

  private List<CallStrategy> strategies;
  private List<AtomicAccessor> accessors;

  private RuntimeOverloadResolver() {
    strategies = new ArrayList<CallStrategy>();
    strategies.add(new FixedArity());
    strategies.add(new UnaryPrimitive(Integer.TYPE));
    strategies.add(new UnaryPrimitive(Double.TYPE));
    strategies.add(new UnaryPrimitive(String.class));
    strategies.add(new BinaryPrimitive());
    strategies.add(new VarArgs());

    // These are essentially the implicit type conversions
    // supported by the R language
    accessors = new ArrayList<AtomicAccessor>();
    accessors.add(new LogicalExpToInt());
    accessors.add(new RealExpToDouble());
    accessors.add(new LogicalExpToDouble());
    accessors.add(new IntExpToDouble());
    accessors.add(new StringExpToString());
    accessors.add(new RealExpToString());
    accessors.add(new IntExpToString());
    accessors.add(new LogicalExpToString());

  }

  public EvalResult invoke(EnvExp rho, LangExp call, List<Method> overloads) {

    // first check for a method which can handle the call in its entirety
    if(overloads.size() == 1 && isLangMethod(overloads.get(0))) {
      return invokeAndWrap(overloads.get(0), rho, call);
    }

    SEXP args[] = evaluateArguments(rho, call.getArguments());

    for(CallStrategy strategy : strategies) {
      for(Method method : overloads) {
        if(strategy.accept(method, args)) {
          return strategy.apply(method, args);
        }
      }
    }
    throw new EvalException(formatErrorMessage(call, overloads));
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
   *
   * <code>
   * () ->  method()
   * (SEXP) ->  method(SEXP)
   * (SEXP, SEXP) ->  method (SEXP, SEXP)
   * (RealExp, RealExp) -> method (RealExp, RealExp)
   * </code>
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

  /**
   * (SEXP...) -> SEXP method(SEXP...)
   */
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
   * (RealExp) -> double method(double, double)
   */
  private class UnaryPrimitive implements CallStrategy {
    private Class primitive;

    private UnaryPrimitive(Class primitive) {
      this.primitive = primitive;
    }

    @Override
    public boolean accept(Method method, SEXP arguments[]) {
      return hasArgs(method, primitive) &&
          arguments.length == 1 &&
          haveAccessor(arguments[0], primitive) &&
          haveResultBuilderFor(method);
    }

    @Override
    public EvalResult apply(Method method, SEXP arguments[]) {
      SEXP domain = arguments[0];
      int length = domain.length();

      AtomicAccessor domainAccessor = getAccessor(domain, primitive);
      AtomicResultBuilder result = resultBuilderFor(method.getReturnType(), length);

      for (int i = 0; i <length; i++) {
        if ( domainAccessor.isNA( domain, i ) ) {
          result.setNA(i);
        } else {
          result.set(i, invoke(method, domainAccessor.get( domain, i )));
        }
      }

      return new EvalResult( result.build() );
    }
  }

  /**
   * Maps a binary primitive function over two vectors.
   *
   */
  private class BinaryPrimitive implements CallStrategy {
    @Override
    public boolean accept(Method method, SEXP arguments[]) {
      return hasArgs(method, Double.TYPE, Double.TYPE) &&
          haveResultBuilderFor(method) &&
          arguments.length == 2 &&
          haveAccessor(arguments[0], Double.TYPE) &&
          haveAccessor(arguments[1], Double.TYPE);
    }

    @Override
    public EvalResult apply(Method method, SEXP arguments[]) {
      SEXP x = arguments[0];
      SEXP y = arguments[1];
      int xLen = x.length();
      int yLen = y.length();
      int maxLen = Math.max(xLen, yLen);
      int minLen = Math.min(xLen, yLen);

      if( maxLen % minLen != 0) {
        throw new EvalException("longer object length is not a multiple of shorter object length");
      }

      AtomicAccessor cx = getAccessor(x, Double.TYPE);
      AtomicAccessor cy = getAccessor(y, Double.TYPE);
      AtomicResultBuilder result = resultBuilderFor(method.getReturnType(), maxLen);

      for(int i=0; i!=maxLen; i++) {
        int xi = i % xLen;
        int yi = i % yLen;

        if( cx.isNA(x, xi) || cy.isNA(y, yi)) {
          result.setNA(i);
        } else {
          result.set(i, invoke(method, cx.get(x, xi), cy.get(y, yi)));
        }
      }

      return new EvalResult( result.build() );
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

    } else if(result instanceof double[]) {
      return new EvalResult( new RealExp((double[]) result) );

    } else if(result instanceof boolean[]) {
      return new EvalResult( new LogicalExp((boolean[]) result));

    } else {
      throw new EvalException("Java function returned incovertable type: " + result.getClass().getName());
    }
  }

  private String formatErrorMessage(LangExp call, List<Method> methods) {
    StringBuilder sb = new StringBuilder();
    sb.append("Cannot execute the function with the arguments supplied.\n");
    sb.append("Arguments: \n\t");
    for(SEXP arg : call.getArguments()) {
      sb.append(arg.getTypeName()).append(" ");
    }

    sb.append("\nAvailable overloads (in ").append(methods.get(0).getDeclaringClass().getName()).append(") :\n");

    for(Method method : methods) {
      sb.append("\t").append(method.getName()).append("(");
      Class<?>[] params = method.getParameterTypes();
      for(int i=0;i!=params.length;++i) {
        if(i>0) {
          sb.append(", ");
        }
        sb.append(params[i].getSimpleName());
      }
      sb.append(")\n");
    }
    return sb.toString();
  }

  private interface AtomicResultBuilder<T> {
    void set(int index, T value);
    void setNA(int index);
    SEXP build();
  }

  private class IntResultBuilder implements AtomicResultBuilder<Integer> {
    private int values[];

    private IntResultBuilder(int length) {
      values = new int[length];
    }

    @Override
    public void set(int index, Integer value) {
      values[index] = value;
    }

    @Override
    public void setNA(int index) {
      values[index] = IntExp.NA;
    }

    @Override
    public SEXP build() {
      return new IntExp(values);
    }
  }

  private class RealResultBuilder implements AtomicResultBuilder<Double> {
    private double values[];

    private RealResultBuilder(int length) {
      values = new double[length];
    }

    @Override
    public void set(int index, Double value) {
      values[index] = value;
    }

    @Override
    public void setNA(int index) {
      values[index] = RealExp.NA;
    }

    @Override
    public SEXP build() {
      return new RealExp( values );
    }
  }

  private static class BooleanResultBuilder implements AtomicResultBuilder<Boolean> {
    private int values[];

    private BooleanResultBuilder(int length) {
      values = new int[length];
    }

    @Override
    public void set(int index, Boolean value) {
      values[index] = value ? 1 : 0;
    }

    @Override
    public void setNA(int index) {
      values[index] = Logical.NA.getInternalValue();
    }

    @Override
    public SEXP build() {
      return new LogicalExp( values );
    }
  }

  private static class StringResultBuilder implements AtomicResultBuilder<String> {

    private String values[];

    public StringResultBuilder(int length) {
      values = new String[length];
    }

    @Override
    public void set(int index, String value) {
      values[index] = value;
    }

    @Override
    public void setNA(int index) {
      values[index] = StringExp.NA;
    }

    @Override
    public SEXP build() {
      return new StringExp(values);
    }
  }


  private boolean haveResultBuilderFor(Method method) {
    Class type = method.getReturnType();
    return
        type == Double.TYPE ||
            type == Integer.TYPE ||
            type == Logical.class ||
            type == Boolean.TYPE ||
            type == String.class;
  }

  private AtomicResultBuilder resultBuilderFor(Class type, int length) {
    if(type == Integer.TYPE) {
      return new IntResultBuilder(length);

    } else if(type == Double.TYPE) {
      return new RealResultBuilder(length);

    } else if(type == Boolean.TYPE) {
      return new BooleanResultBuilder(length);

    } else if(type == String.class) {
      return new StringResultBuilder(length);

    } else {
      throw new UnsupportedOperationException("No AtomicResultBuilder for " + type.getName() );
    }
  }


  /**
   * Interface for objects which converts individual elements
   * to destination types.
   * @param <S>
   * @param <D>
   */
  private interface AtomicAccessor<S extends SEXP, D> {
    boolean accept(Class<? extends SEXP> expType, Class destinationType);
    boolean isNA(S exp, int index);
    D get(S exp, int index);
  }

  private static class LogicalExpToInt implements AtomicAccessor<LogicalExp, Integer> {
    @Override
    public boolean accept(Class<? extends SEXP> expType, Class destinationType) {
      return expType == LogicalExp.class && destinationType == Integer.TYPE;
    }

    @Override
    public boolean isNA(LogicalExp exp, int index) {
      return IntExp.isNA( exp.get(index) );
    }

    @Override
    public Integer get(LogicalExp exp, int index) {
      return exp.get(index);
    }
  }

  private static class RealExpToDouble implements AtomicAccessor<RealExp, Double> {
    @Override
    public boolean accept(Class<? extends SEXP> expType, Class destinationType) {
      return expType == RealExp.class && destinationType == Double.TYPE;
    }

    @Override
    public boolean isNA(RealExp exp, int index) {
      return RealExp.isNA( exp.get(index) );
    }

    @Override
    public Double get(RealExp exp, int index) {
      return exp.get(index);
    }
  }

  private static class IntExpToDouble implements AtomicAccessor<IntExp, Double> {
    @Override
    public boolean accept(Class<? extends SEXP> expType, Class destinationType) {
      return expType == IntExp.class && destinationType == Double.TYPE;
    }

    @Override
    public boolean isNA(IntExp exp, int index) {
      return IntExp.isNA( exp.get( index ) );
    }

    @Override
    public Double get(IntExp exp, int index) {
      return (double) exp.get( index );
    }
  }

  private static class LogicalExpToDouble implements AtomicAccessor<LogicalExp, Double> {
    @Override
    public boolean accept(Class<? extends SEXP> expType, Class destinationType) {
      return expType == LogicalExp.class && destinationType == Double.TYPE;
    }

    @Override
    public boolean isNA(LogicalExp exp, int index) {
      return exp.get( index ) == IntExp.NA;
    }

    @Override
    public Double get(LogicalExp exp, int index) {
      return (double) exp.get( index );
    }
  }

  private static class StringExpToString implements AtomicAccessor<StringExp, String> {
    @Override
    public boolean accept(Class<? extends SEXP> expType, Class destinationType) {
      return expType == StringExp.class && destinationType == String.class;
    }

    @Override
    public boolean isNA(StringExp exp, int index) {
      return StringExp.isNA( exp.get(index) );
    }

    @Override
    public String get(StringExp exp, int index) {
      return exp.get(index);
    }
  }

  private static class RealExpToString implements AtomicAccessor<RealExp, String> {
    @Override
    public boolean accept(Class<? extends SEXP> expType, Class destinationType) {
      return expType == RealExp.class && destinationType == String.class;
    }

    @Override
    public boolean isNA(RealExp exp, int index) {
      return RealExp.isNA(exp.get(index));
    }

    @Override
    public String get(RealExp exp, int index) {
      return ParseUtil.toString(exp.get(index));
    }
  }

  private static class IntExpToString implements AtomicAccessor<IntExp, String> {
    @Override
    public boolean accept(Class<? extends SEXP> expType, Class destinationType) {
      return expType == IntExp.class && destinationType == String.class;
    }

    @Override
    public boolean isNA(IntExp exp, int index) {
      return IntExp.isNA( exp.get( index ) );
    }

    @Override
    public String get(IntExp exp, int index) {
      return ParseUtil.toString( exp.get( index ) );
    }
  }

  private static class LogicalExpToString implements AtomicAccessor<LogicalExp, String> {
    @Override
    public boolean accept(Class<? extends SEXP> expType, Class destinationType) {
      return expType == LogicalExp.class && destinationType == String.class ;
    }

    @Override
    public boolean isNA(LogicalExp exp, int index) {
      return IntExp.isNA( exp.get( index ) );
    }

    @Override
    public String get(LogicalExp exp, int index) {
      return exp.get( index ) == 1 ? "TRUE" : "FALSE";
    }
  }

  private AtomicAccessor getAccessor(SEXP exp, Class primitiveType) {
    for(AtomicAccessor converter : accessors) {
      if(converter.accept(exp.getClass(), primitiveType)) {
        return converter;
      }
    }
    return null;
  }

  private boolean haveAccessor(SEXP exp, Class primitiveType) {
    return getAccessor(exp, primitiveType) != null;
  }
}
