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

package r.lang.primitive.binding;

import com.google.common.collect.Lists;
import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.AllowNA;
import r.lang.primitive.annotations.Indices;

import java.lang.Class;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
  private List<ArgConverter> converters;

  private RuntimeInvoker() {
    strategies = new ArrayList<CallStrategy>();
    strategies.add(new UnaryPrimitive(Integer.TYPE));
    strategies.add(new UnaryPrimitive(Double.TYPE));
    strategies.add(new UnaryPrimitive(String.class));
    strategies.add(new UnaryPrimitive(Boolean.TYPE));
    strategies.add(new BinaryPrimitive(Integer.TYPE));
    strategies.add(new BinaryPrimitive(Double.TYPE));
    strategies.add(new BinaryPrimitive(String.class));
    strategies.add(new FixedArity());
    strategies.add(new VarArgs());

    // converters between whole expressions and
    // argument types
    converters = new ArrayList<ArgConverter>();
    converters.add(new ToPrimitive());
    converters.add(new StringToSymbol());
    converters.add(new FromExternalPtr());
    converters.add(new DoubleToIndices());
    converters.add(new DoubleToInt());
    converters.add(new IntToIndices());
    converters.add(new NullToObject());
  }

  public EvalResult invoke(Environment rho, FunctionCall call, List<PrimitiveMethod> overloads) {

    // first check for a method which can handle the call in its entirety
    if(overloads.size() == 1 && overloads.get(0).acceptsCall()) {
      return overloads.get(0).invokeAndWrap(rho, call);
    }

    // make a list of the provided arguments
    List<ProvidedArgument> provided = Lists.newArrayList();
    for(PairList.Node arg : call.getArguments().nodes()) {
      if(SymbolExp.ELLIPSES.equals(arg.getValue())) {
        // the values of the '...' are just merged into the argument list
        DotExp ellipses = (DotExp) arg.getValue().evalToExp(rho);
        for(PairList.Node dotArg : ellipses.getPromises().nodes()) {
          provided.add(new ProvidedArgument(rho, dotArg));
        }
      } else {
        provided.add(new ProvidedArgument(rho, arg));
      }
    }

    // do we have a single method that accepts the whole argument list?
    if(overloads.size() == 1 && overloads.get(0).acceptsArgumentList()) {
      return overloads.get(0).invokeAndWrap(toEvaluatedPairList(provided));
    }

    for(CallStrategy strategy : strategies) {
      for(PrimitiveMethod method : overloads) {
        if(strategy.accept(method, provided)) {
          return strategy.apply(method, rho, provided);
        }
      }
    }

    throw new EvalException(formatNoMatchingOverloadMessage(call, provided, overloads));
  }



  private interface CallStrategy {
    boolean accept(PrimitiveMethod method, List<ProvidedArgument> arguments);
    EvalResult apply(PrimitiveMethod method, Environment rho, List<ProvidedArgument> arguments);
  }

  /**
   * Evaluates and passes a fixed number of {@code SEXP}s
   * to the given method.
   *
   * <code>
   * () ->  method()
   * (SEXP) ->  method(SEXP)
   * (SEXP, SEXP) ->  method (SEXP, SEXP)
   * (DoubleVector, DoubleVector) -> method (DoubleVector, DoubleVector)
   * </code>
   */
  private class FixedArity implements CallStrategy {

    @Override
    public boolean accept(PrimitiveMethod method, List<ProvidedArgument> providedArgs) {
      return acceptArguments(providedArgs, method.getFormals());
    }

    @Override
    public EvalResult apply(PrimitiveMethod method, Environment rho, List<ProvidedArgument> arguments) {
      return method.invokeWithContextAndWrap(rho, convertArgs(method, arguments));
    }
  }

  /**
   * (SEXP...) -> SEXP method(SEXP...)
   */
  private class VarArgs implements CallStrategy {

    @Override
    public boolean accept(PrimitiveMethod method, List<ProvidedArgument> arguments) {
      return method.argumentListEquals(SEXP[].class);
    }

    @Override
    public EvalResult apply(PrimitiveMethod method, Environment rho, List<ProvidedArgument> arguments) {
      return method.invokeAndWrap(new Object[] { arguments });
    }
  }
  private PairList toEvaluatedPairList(List<ProvidedArgument> arguments) {
    PairList.Node.Builder builder = PairList.Node.newBuilder();
    for(ProvidedArgument arg : arguments) {
      builder.add(arg.getTag(), arg.evaluated());
    }
    return builder.build();
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
    public boolean accept(PrimitiveMethod method, List<ProvidedArgument> arguments) {
      return method.argumentListEquals(primitive) &&
          arguments.size() == 1 &&
          AtomicAccessors.haveAccessor(arguments.get(0).evaluated(), primitive) &&
          AtomicBuilders.haveFor(method);
    }

    @Override
    public EvalResult apply(PrimitiveMethod method, Environment rho, List<ProvidedArgument> arguments) {
      AtomicAccessor domain =  AtomicAccessors.create( arguments.get(0).evaluated(), primitive );
      AtomicBuilder result = AtomicBuilders.createFor(method.getReturnType(), domain.length() );
      boolean allowNA = method.getArguments().get(0).isAnnotatedWith(AllowNA.class);


      for (int i = 0; i < domain.length(); i++) {
        if ( !allowNA && domain.isNA(i) ) {
          result.setNA(i);
        } else {
          result.set(i, method.invoke( domain.get( i )));
        }
      }

      return new EvalResult( result.build() );
    }
  }

  private Object[] convertArgs(PrimitiveMethod method, List<ProvidedArgument> providedArgs) {
    Object newArray[] = new Object[providedArgs.size()];
    for(int i=0;i!=providedArgs.size();++i) {
      PrimitiveMethod.Argument formal = method.getFormals().get(i);
      ProvidedArgument provided = providedArgs.get(i);

      newArray[i] = provided.convertTo(formal);
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
    public boolean accept(PrimitiveMethod method, List<ProvidedArgument> arguments) {
      return
          method.argumentListEquals(primitiveType, primitiveType) &&
              AtomicBuilders.haveFor(method) &&
              arguments.size() == 2 &&
              AtomicAccessors.haveAccessor(arguments.get(0).evaluated(), primitiveType) &&
              AtomicAccessors.haveAccessor(arguments.get(1).evaluated(), primitiveType);
    }

    @Override
    public EvalResult apply(PrimitiveMethod method, Environment rho, List<ProvidedArgument> arguments) {
      AtomicAccessor<Double> x = AtomicAccessors.create( arguments.get(0).evaluated(), primitiveType );
      AtomicAccessor<Double> y = AtomicAccessors.create( arguments.get(1).evaluated(), primitiveType );
      int xLen = x.length();
      int yLen = y.length();
      int maxLen = Math.max(xLen, yLen);
      int minLen = Math.min(xLen, yLen);

      if( maxLen % minLen != 0) {
        throw new EvalException("longer object length is not a multiple of shorter object length");
      }

      AtomicBuilder result = AtomicBuilders.createFor(method.getReturnType(), maxLen);

      for(int i=0; i!=maxLen; i++) {
        int xi = i % xLen;
        int yi = i % yLen;

        if( x.isNA(xi) || y.isNA(yi)) {
          result.setNA(i);
        } else {
          result.set(i, method.invoke(x.get(xi), y.get(yi)));
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
    return has(annotations, expectedAnnotation);
  }

  private static boolean has(Annotation[] annotations, Class<? extends Annotation> expectedAnnotation) {
    for(Annotation annotation : annotations) {
      if(annotation.annotationType() == expectedAnnotation) {
        return true;
      }
    }
    return false;
  }

  private boolean acceptArguments(List<ProvidedArgument> provided, List<PrimitiveMethod.Argument> formals) {
    if(provided.size() != formals.size()) {
      return false;
    }
    for(int i=0; i!= formals.size(); ++i) {
      if(!provided.get(i).canBePassedTo(formals.get((i)))){
        return false;
      }
    }
    return true;
  }

  private String formatNoMatchingOverloadMessage(FunctionCall call, List<ProvidedArgument> provided, List<PrimitiveMethod> methods) {
    StringBuilder sb = new StringBuilder();
    sb.append("Cannot execute the function with the arguments supplied.\n");
    appendProvidedArguments(sb, provided);

    sb.append("\nAvailable overloads (in ").append(methods.get(0).getDeclaringClass().getName()).append(") :\n");

    appendOverloadsTo(methods, sb);

    return sb.toString();
  }

  private void appendProvidedArguments(StringBuilder sb, List<ProvidedArgument> provided) {
    sb.append("Arguments: \n\t");
    for(ProvidedArgument arg : provided) {
      sb.append(arg.getTypeName()).append(" ");
    }
  }

  private void appendOverloadsTo(List<PrimitiveMethod> methods, StringBuilder sb) {
    for(PrimitiveMethod method : methods) {
      sb.append("\t");
      method.appendFriendlySignatureTo(sb);
      sb.append("\n");
    }
  }
  private class ProvidedArgument {
    private Environment rho;
    private SEXP provided;
    private SEXP evaluated;
    private SEXP tag;

    public ProvidedArgument(Environment rho, PairList.Node arg) {
      this.rho = rho;
      this.provided = arg.getValue();
      this.tag = arg.getRawTag();
    }

    public boolean canBePassedTo(PrimitiveMethod.Argument formal) {
      if(formal.isEvaluated()) {
        return canBePassedTo(evaluated(), formal);
      } else {
        return canBePassedTo(provided, formal);
      }
    }

    private SEXP evaluated() {
      if(evaluated == null ) {
        evaluated = provided.evaluate(rho).getExpression();
        if(evaluated instanceof Promise) {
          evaluated = evaluated.evalToExp(rho);
        }
      }
      return evaluated;
    }

    private boolean canBePassedTo(SEXP provided, PrimitiveMethod.Argument formal) {
      if(formal.isAssignableFrom(provided)) {
        return true;
      } else {
        return haveConverter(provided, formal);
      }
    }

    public Object convertTo(PrimitiveMethod.Argument formal) {
      SEXP value;
      if(formal.isEvaluated()) {
        value = evaluated();
      } else {
        value = provided;
      }

      if(formal.isAssignableFrom(value)) {
        return value;
      }

      return getConverter(value, formal).convert(rho, value, formal);
    }

    public String getTypeName() {
      return evaluated().getTypeName();
    }

    public SEXP getTag() {
      return tag;
    }
  }

  private interface ArgConverter<S extends SEXP, T> {
    boolean accept(SEXP source, PrimitiveMethod.Argument formal);
    T convert(Environment rho, S source, PrimitiveMethod.Argument formal);
  }

  private class ToPrimitive implements ArgConverter {

    @Override
    public boolean accept(SEXP source, PrimitiveMethod.Argument formal) {
      return source.length() == 1 && AtomicAccessors.haveAccessor(source, formal.getClazz());
    }

    @Override
    public Object convert(Environment rho, SEXP source, PrimitiveMethod.Argument formal) {
      return AtomicAccessors.create(source, formal.getClazz()).get(0);
    }
  }

  private class StringToSymbol implements ArgConverter<StringVector, SymbolExp> {

    @Override
    public boolean accept(SEXP source, PrimitiveMethod.Argument formal) {
      return source instanceof StringVector &&
          source.length() == 1 &&
          formal.isSymbol();
    }

    @Override
    public SymbolExp convert(Environment rho, StringVector source, PrimitiveMethod.Argument formal) {
      return new SymbolExp(source.getElement(0));
    }
  }

  private class DoubleToIndices implements ArgConverter<DoubleVector, int[]> {
    @Override
    public boolean accept(SEXP source, PrimitiveMethod.Argument formal) {
      return
          source instanceof DoubleVector &&
          formal.isAnnotatedWith(Indices.class) &&
          formal.getClazz().equals(int[].class);
    }

    @Override
    public int[] convert(Environment rho, DoubleVector source, PrimitiveMethod.Argument formal) {
      return source.coerceToIntArray();
    }
  }

  private class IntToIndices implements ArgConverter<IntVector,  int[]> {
    @Override
    public boolean accept(SEXP source, PrimitiveMethod.Argument formal) {
      return
          source instanceof IntVector &&
          formal.isAnnotatedWith(Indices.class) &&
          formal.getClazz().equals(int[].class);
    }

    @Override
    public int[] convert(Environment rho, IntVector source, PrimitiveMethod.Argument formal) {
      return source.toIntArray();
    }
  }

  private class DoubleToInt implements ArgConverter<DoubleVector, Integer> {
    @Override
    public boolean accept(SEXP source, PrimitiveMethod.Argument formal) {
      return source instanceof DoubleVector &&
          source.length() == 1 &&
          formal.isAnnotatedWith(Indices.class) &&
          formal.getClazz().equals(Integer.TYPE);
    }

    @Override
    public Integer convert(Environment rho, DoubleVector source, PrimitiveMethod.Argument formal) {
      return (int)source.get(0);
    }
  }

  private boolean haveConverter(SEXP source, PrimitiveMethod.Argument formal) {
    return getConverter(source,formal) != null;
  }

  private ArgConverter getConverter(SEXP source, PrimitiveMethod.Argument formal) {
    for(ArgConverter converter : converters) {
      if(converter.accept(source, formal)) {
        return converter;
      }
    }
    return null;
  }


  private class FromExternalPtr implements ArgConverter {

    @Override
    public boolean accept(SEXP source, PrimitiveMethod.Argument formal) {
      return source instanceof ExternalExp && formal.getClazz().isAssignableFrom(
          ((ExternalExp) source).getValue().getClass());
    }

    @Override
    public Object convert(Environment rho, SEXP source, PrimitiveMethod.Argument formal) {
      return ((ExternalExp)source).getValue();
    }
  }

  private class NullToObject implements ArgConverter {
    @Override
    public boolean accept(SEXP source, PrimitiveMethod.Argument formal) {
      return source == Null.INSTANCE && !formal.getClazz().isPrimitive();
    }

    @Override
    public Object convert(Environment rho, SEXP source, PrimitiveMethod.Argument formal) {
      return null;
    }
  }
}
