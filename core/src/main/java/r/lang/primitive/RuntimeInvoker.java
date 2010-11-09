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

package r.lang.primitive;

import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.Environment;
import r.parser.ParseUtil;

import java.lang.Boolean;
import java.lang.Class;
import java.lang.Double;
import java.lang.IllegalAccessException;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.UnsupportedOperationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Invokes a JVM method from the R language.
 *
 * This class handles all the details of mapping an R call to a static method in the JVM,
 * applying implicit type conversions, evaluating arguments, etc.
 *
 * This implementation relies heavily on reflection, so it will probably be several orders
 * of magnitude too slow, but it does succeed in extracting all the messy details of type conversion
 * away from the primitive implementations.
 *
 * In the future, this could be replaced by a code generator that generates wrappers for each overload,
 * or, more ambitiously, just-in-time wrapper generation.
 */
public class RuntimeInvoker {

  public static final RuntimeInvoker INSTANCE = new RuntimeInvoker();

  private List<CallStrategy> strategies;
  private List<AtomicAccessor> accessors;
  private Map<Class, String> rTypeNames;

  private RuntimeInvoker() {
    strategies = new ArrayList<CallStrategy>();
    strategies.add(new UnaryPrimitive(Integer.TYPE));
    strategies.add(new UnaryPrimitive(Double.TYPE));
    strategies.add(new UnaryPrimitive(String.class));
    strategies.add(new BinaryPrimitive(Integer.TYPE));
    strategies.add(new BinaryPrimitive(Double.TYPE));
    strategies.add(new BinaryPrimitive(String.class));
    strategies.add(new FixedArity());
    strategies.add(new FixedArityWithEnvironment());
    strategies.add(new VarArgs());

    accessors = new ArrayList<AtomicAccessor>();
    accessors.add(new LogicalExpToBoolean());
    accessors.add(new IntExpAccessor());
    accessors.add(new RealExpAccessor());
    accessors.add(new StringExpAccessor());

    // These are essentially the implicit type conversions
    // supported by the R language
    accessors.add(new LogicalExpToInt());
    accessors.add(new LogicalExpToDouble());
    accessors.add(new IntExpToDouble());
    accessors.add(new RealExpToString());
    accessors.add(new IntExpToString());
    accessors.add(new LogicalExpToString());

    // friendly names for printing JVM types
    rTypeNames = new HashMap<Class, String>();
    rTypeNames.put(SEXP[].class, "...");
    rTypeNames.put(SEXP.class, "any");
    rTypeNames.put(LogicalExp.class, LogicalExp.TYPE_NAME);
    rTypeNames.put(Logical.class, LogicalExp.TYPE_NAME);
    rTypeNames.put(Boolean.class, LogicalExp.TYPE_NAME);
    rTypeNames.put(Boolean.TYPE, LogicalExp.TYPE_NAME);
    rTypeNames.put(IntExp.class, IntExp.TYPE_NAME);
    rTypeNames.put(Integer.class, IntExp.TYPE_NAME);
    rTypeNames.put(Integer.TYPE, IntExp.TYPE_NAME);
    rTypeNames.put(DoubleExp.class, DoubleExp.TYPE_NAME);
    rTypeNames.put(Double.class, DoubleExp.TYPE_NAME);
    rTypeNames.put(Double.TYPE, DoubleExp.TYPE_NAME);
    rTypeNames.put(String.class, StringExp.TYPE_NAME);
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
          return strategy.apply(method, rho, args);
        }
      }
    }
    throw new EvalException(formatErrorMessage(call, args, overloads));
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
      evaluatedArguments[i] = arg.evaluate(rho).getExpression();
      if(evaluatedArguments[i] instanceof PromiseExp) {
        evaluatedArguments[i] = evaluatedArguments[i].evalToExp(rho);
      }
      i++;    
    }
    return evaluatedArguments;
  }


  private interface CallStrategy {
    boolean accept(Method method, SEXP arguments[]);
    EvalResult apply(Method method, EnvExp rho, SEXP arguments[]);
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
      return acceptArguments(providedArgs, method.getParameterTypes());
    }

    @Override
    public EvalResult apply(Method method, EnvExp rho, SEXP arguments[]) {
      return invokeAndWrap(method, convertArgs(method, arguments, 0));
    }
  }

  private class FixedArityWithEnvironment implements CallStrategy {
    @Override
    public boolean accept(Method method, SEXP[] arguments) {
      return isArgAnnotated(method, 0, Environment.class) &&
          acceptArguments(arguments, skip(method.getParameterTypes(), 1));
    }

    @Override
    public EvalResult apply(Method method, EnvExp rho, SEXP[] arguments) {
      return invokeAndWrap(method, concat(rho, convertArgs(method, arguments, 1)));
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
          method.getParameterTypes()[0] == SEXP[].class;
    }

    @Override
    public EvalResult apply(Method method, EnvExp rho, SEXP[] arguments) {
      return invokeAndWrap(method, new Object[] { arguments });
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
    public EvalResult apply(Method method, EnvExp rho, SEXP arguments[]) {
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

  private Object[] convertArgs(Method method, SEXP[] args, int skip) {
    Object newArray[] = new Object[args.length];
    for(int i=skip;i<args.length;++i) {
      Class type = method.getParameterTypes()[i-skip];
      if(type.isAssignableFrom(args[i].getClass())) {
        newArray[i] = args[i];
      } else {
        newArray[i] = getAccessor(args[i], type).get(args[i], 0);
      }
    }
    return newArray;
  }

  /**
   * Maps a binary primitive function over two vectors.
   *
   */
  private class BinaryPrimitive implements CallStrategy {
    private Class<Double> primitiveType;

    public BinaryPrimitive(Class type) {
      primitiveType = type;
    }

    @Override
    public boolean accept(Method method, SEXP arguments[]) {
      return hasArgs(method, primitiveType, primitiveType) &&
          haveResultBuilderFor(method) &&
          arguments.length == 2 &&
          haveAccessor(arguments[0], primitiveType) &&
          haveAccessor(arguments[1], primitiveType);
    }

    @Override
    public EvalResult apply(Method method, EnvExp rho, SEXP arguments[]) {
      SEXP x = arguments[0];
      SEXP y = arguments[1];
      int xLen = x.length();
      int yLen = y.length();
      int maxLen = Math.max(xLen, yLen);
      int minLen = Math.min(xLen, yLen);

      if( maxLen % minLen != 0) {
        throw new EvalException("longer object length is not a multiple of shorter object length");
      }

      AtomicAccessor cx = getAccessor(x, primitiveType);
      AtomicAccessor cy = getAccessor(y, primitiveType);
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

  private static boolean isArgAnnotated(Method method, int index, Class<? extends Annotation> expectedAnnotation) {
    if( index >= method.getParameterAnnotations().length) {
      return false;
    }
    Annotation[] annotations = method.getParameterAnnotations()[index];
    for(Annotation annotation : annotations) {
      if(annotation.annotationType() == expectedAnnotation) {
        return true;
      }
    }
    return false;
  }

  private boolean acceptArguments(SEXP[] provided, Class<?>[] expected) {
    if(provided.length != expected.length) {
      return false;
    }
    for(int i=0; i!= expected.length; ++i) {
      if(!acceptArgument(provided[i], expected[i])) {
        return false;
      }
    }
    return true;
  }

  private boolean acceptArgument(SEXP provided, Class expected) {
    if(expected.isAssignableFrom(provided.getClass())) {
      return true;
    }
    if(haveAccessor(provided, expected) && provided.length() == 1) {
      return true;
    }
    return false;
  }


  private static <T> T[] skip(T[] array, int index) {
    return Arrays.copyOfRange(array, index, array.length);
  }

  private static Object[] concat(Object first, Object... elements) {
    Object newArray[] = new Object[elements.length+1];
    newArray[0] = first;
    java.lang.System.arraycopy(elements, 0, newArray, 1, elements.length);
    return newArray;
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
    } catch(IllegalArgumentException e) {
      throw new RuntimeException("IllegalArgumentException while invoking " + overload.toString());
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
      return new EvalResult( new DoubleExp(((Long)result).doubleValue()) );

    } else if(result instanceof Double) {
      return new EvalResult( new DoubleExp( (Double) result ));

    } else if(result instanceof Boolean) {
      return new EvalResult( new LogicalExp( (Boolean) result ));

    } else if(result instanceof Integer) {
      return new EvalResult( new IntExp( (Integer) result));
      
    } else if(result instanceof String) {
      return new EvalResult( new StringExp( (String) result ));

    } else if(result instanceof double[]) {
      return new EvalResult( new DoubleExp((double[]) result) );

    } else if(result instanceof boolean[]) {
      return new EvalResult( new LogicalExp((boolean[]) result));

    } else {
      throw new EvalException("JVM method returned incovertable type: " + result.getClass().getName());
    }
  }

  private String formatErrorMessage(LangExp call, SEXP[] args, List<Method> methods) {
    StringBuilder sb = new StringBuilder();
    sb.append("Cannot execute the function with the arguments supplied.\n");
    sb.append("Arguments: \n\t");
    for(SEXP arg : args) {
      sb.append(arg.getTypeName()).append(" ");
    }

    sb.append("\nAvailable overloads (in ").append(methods.get(0).getDeclaringClass().getName()).append(") :\n");

    for(Method method : methods) {
      sb.append("\t").append(method.getName()).append("(");
      Class<?>[] params = method.getParameterTypes();
      boolean needsComma=false;
      for(int i=0;i!=params.length;++i) {
        if(!isArgAnnotated(method,0,Environment.class)) {
          if(needsComma) {
            sb.append(", ");
          } else {
            needsComma=true;
          }
          if(rTypeNames.containsKey(params[i])) {
            sb.append(rTypeNames.get(params[i]));
          } else { 
            sb.append(params[i].getSimpleName());
          }
        }
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
      values[index] = DoubleExp.NA;
    }

    @Override
    public SEXP build() {
      return new DoubleExp( values );
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

  private class IntExpAccessor implements AtomicAccessor<IntExp, Integer> {
    @Override
    public boolean accept(Class<? extends SEXP> expType, Class destinationType) {
      return expType == IntExp.class && destinationType == Integer.TYPE;
    }

    @Override
    public boolean isNA(IntExp exp, int index) {
      return exp.get(index) == IntExp.NA;
    }

    @Override
    public Integer get(IntExp exp, int index) {
      return exp.get(index);
    }
  }

  private static class RealExpAccessor implements AtomicAccessor<DoubleExp, Double> {
    @Override
    public boolean accept(Class<? extends SEXP> expType, Class destinationType) {
      return expType == DoubleExp.class && destinationType == Double.TYPE;
    }

    @Override
    public boolean isNA(DoubleExp exp, int index) {
      return DoubleExp.isNA( exp.get(index) );
    }

    @Override
    public Double get(DoubleExp exp, int index) {
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

  private static class LogicalExpToBoolean implements AtomicAccessor<LogicalExp, Boolean> {
    @Override
    public boolean accept(Class<? extends SEXP> expType, Class destinationType) {
      return expType == LogicalExp.class && destinationType == Boolean.TYPE;
    }

    @Override
    public boolean isNA(LogicalExp exp, int index) {
      return exp.get(index) == IntExp.NA;
    }

    @Override
    public Boolean get(LogicalExp exp, int index) {
      return exp.get(index) == 1;
    }
  }

  private static class StringExpAccessor implements AtomicAccessor<StringExp, String> {
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

  private static class RealExpToString implements AtomicAccessor<DoubleExp, String> {
    @Override
    public boolean accept(Class<? extends SEXP> expType, Class destinationType) {
      return expType == DoubleExp.class && destinationType == String.class;
    }

    @Override
    public boolean isNA(DoubleExp exp, int index) {
      return DoubleExp.isNA(exp.get(index));
    }

    @Override
    public String get(DoubleExp exp, int index) {
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
