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
import r.parser.ParseUtil;

import java.lang.Boolean;
import java.lang.Class;
import java.lang.Double;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.UnsupportedOperationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
  private List<AtomicAccessor> accessors;
  private List<ExpConverter> converters;

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
    strategies.add(new PairListArgs());

    accessors = new ArrayList<AtomicAccessor>();
    accessors.add(new LogicalExpToBoolean());
    accessors.add(new IntExpAccessor());
    accessors.add(new RealExpAccessor());
    accessors.add(new StringExpAccessor());

    // These are essentially the implicit type conversions
    // between primitive functions supported by the R language
    accessors.add(new LogicalExpToInt());
    accessors.add(new LogicalExpToDouble());
    accessors.add(new IntExpToDouble());
    accessors.add(new RealExpToString());
    accessors.add(new IntExpToString());
    accessors.add(new LogicalExpToString());

    // converters between whole expressions and
    // argument types
    converters = new ArrayList<ExpConverter>();
    converters.add(new ToPrimitive());
    converters.add(new StringToSymbol());

  }

  public EvalResult invoke(EnvExp rho, LangExp call, List<PrimitiveMethod> overloads) {

    // first check for a method which can handle the call in its entirety
    if(overloads.size() == 1 && overloads.get(0).isLanguage()) {
      return overloads.get(0).invokeAndWrap(rho, call);
    }

    // make a list of the provided arguments
    List<ProvidedArgument> provided = Lists.newArrayList();
    for(PairListExp arg : call.getArguments().listNodes()) {
      provided.add(new ProvidedArgument(rho, arg));
    }

    for(CallStrategy strategy : strategies) {
      for(PrimitiveMethod method : overloads) {
        if(strategy.accept(method, provided)) {
          return strategy.apply(method, rho, provided);
        }
      }
    }
    throw new EvalException(formatErrorMessage(call, provided, overloads));
  }



  private interface CallStrategy {
    boolean accept(PrimitiveMethod method, List<ProvidedArgument> arguments);
    EvalResult apply(PrimitiveMethod method, EnvExp rho, List<ProvidedArgument> arguments);
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
    public boolean accept(PrimitiveMethod method, List<ProvidedArgument> providedArgs) {
      return acceptArguments(providedArgs, method.getArguments());
    }

    @Override
    public EvalResult apply(PrimitiveMethod method, EnvExp rho, List<ProvidedArgument> arguments) {
      return method.invokeAndWrap(convertArgs(method, arguments, 0));
    }
  }

  private class FixedArityWithEnvironment implements CallStrategy {
    @Override
    public boolean accept(PrimitiveMethod method, List<ProvidedArgument> arguments) {
      return method.getArguments().get(0).isEnvironment() &&
          acceptArguments(arguments,method.getFormals());
    }

    @Override
    public EvalResult apply(PrimitiveMethod method, EnvExp rho, List<ProvidedArgument> arguments) {
      return method.invokeAndWrap(concat(rho, convertArgs(method, arguments, 1)));
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
    public EvalResult apply(PrimitiveMethod method, EnvExp rho, List<ProvidedArgument> arguments) {
      return method.invokeAndWrap(new Object[] { arguments });
    }
  }

  private class PairListArgs implements CallStrategy {
    @Override
    public boolean accept(PrimitiveMethod method, List<ProvidedArgument> arguments) {
      return method.argumentListEquals(PairList.class);
    }

    @Override
    public EvalResult apply(PrimitiveMethod method, EnvExp rho, List<ProvidedArgument> arguments) {
      PairListExp.Builder builder = PairListExp.buildList();
      for(ProvidedArgument arg : arguments) {
        builder.add(arg.evaluated()).taggedWith(arg.getTag());
      }
      return method.invokeAndWrap(builder.list());
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
    public boolean accept(PrimitiveMethod method, List<ProvidedArgument> arguments) {
      return method.argumentListEquals(primitive) &&
          arguments.size() == 1 &&
          haveAccessor(arguments.get(0).evaluated(), primitive) &&
          haveResultBuilderFor(method);
    }

    @Override
    public EvalResult apply(PrimitiveMethod method, EnvExp rho, List<ProvidedArgument> arguments) {
      SEXP domain = arguments.get(0).evaluated();
      int length = domain.length();

      AtomicAccessor domainAccessor = getAccessor(domain, primitive);
      AtomicResultBuilder result = resultBuilderFor(method.getReturnType(), length);

      for (int i = 0; i <length; i++) {
        if ( domainAccessor.isNA( domain, i ) ) {
          result.setNA(i);
        } else {
          result.set(i, method.invoke( domainAccessor.get( domain, i )));
        }
      }

      return new EvalResult( result.build() );
    }
  }

  private Object[] convertArgs(PrimitiveMethod method, List<ProvidedArgument> providedArgs, int skip) {
    Object newArray[] = new Object[providedArgs.size()];
    for(int i=skip;i<providedArgs.size();++i) {
      PrimitiveMethod.Argument formal = method.getArguments().get(i-skip);
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
              haveResultBuilderFor(method) &&
              arguments.size() == 2 &&
              haveAccessor(arguments.get(0).evaluated(), primitiveType) &&
              haveAccessor(arguments.get(1).evaluated(), primitiveType);
    }

    @Override
    public EvalResult apply(PrimitiveMethod method, EnvExp rho, List<ProvidedArgument> arguments) {
      SEXP x = arguments.get(0).evaluated();
      SEXP y = arguments.get(1).evaluated();
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
          result.set(i, method.invoke(cx.get(x, xi), cy.get(y, yi)));
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




  private static <T> T[] skip(T[] array, int index) {
    return Arrays.copyOfRange(array, index, array.length);
  }

  private static Object[] concat(Object first, Object... elements) {
    Object newArray[] = new Object[elements.length+1];
    newArray[0] = first;
    java.lang.System.arraycopy(elements, 0, newArray, 1, elements.length);
    return newArray;
  }



  private String formatErrorMessage(LangExp call, List<ProvidedArgument> provided, List<PrimitiveMethod> methods) {
    StringBuilder sb = new StringBuilder();
    sb.append("Cannot execute the function with the arguments supplied.\n");
    sb.append("Arguments: \n\t");
    for(ProvidedArgument arg : provided) {
      sb.append(arg.getTypeName()).append(" ");
    }

    sb.append("\nAvailable overloads (in ").append(methods.get(0).getDeclaringClass().getName()).append(") :\n");

    for(PrimitiveMethod method : methods) {
      sb.append("\t");
      method.appendFriendlySignatureTo(sb);
      sb.append("\n");
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


  private boolean haveResultBuilderFor(PrimitiveMethod method) {
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

  private AtomicAccessor getAccessor(SEXP provided, Class primitiveType) {
    for(AtomicAccessor converter : accessors) {
      if(converter.accept(provided.getClass(), primitiveType)) {
        return converter;
      }
    }
    return null;
  }

  private boolean haveAccessor(SEXP provided, Class primitiveType) {
    return getAccessor(provided, primitiveType) != null;
  }

  private class ProvidedArgument {
    private EnvExp rho;
    private SEXP provided;
    private SEXP evaluated;
    private SEXP tag;

    public ProvidedArgument(EnvExp rho, PairListExp arg) {
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
        if(evaluated instanceof PromiseExp) {
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

    public Class getEvaledClass() {
      return evaluated().getClass();
    }

    public SEXP getTag() {
      return tag;
    }
  }

  private interface ExpConverter<S extends SEXP, T> {
    boolean accept(SEXP source, PrimitiveMethod.Argument formal);
    T convert(EnvExp rho, S source, PrimitiveMethod.Argument formal);
  }

  private class ToPrimitive implements ExpConverter {

    @Override
    public boolean accept(SEXP source, PrimitiveMethod.Argument formal) {
      return source.length() == 1 && haveAccessor(source, formal.getClazz());
    }

    @Override
    public Object convert(EnvExp rho, SEXP source, PrimitiveMethod.Argument formal) {
      return getAccessor(source, formal.getClazz()).get(source, 0);
    }
  }

  private class StringToSymbol implements ExpConverter<StringExp, SymbolExp> {

    @Override
    public boolean accept(SEXP source, PrimitiveMethod.Argument formal) {
      return source instanceof StringExp &&
          source.length() == 1 &&
          formal.isSymbol();
    }

    @Override
    public SymbolExp convert(EnvExp rho, StringExp source, PrimitiveMethod.Argument formal) {
      return rho.getGlobalContext().getSymbolTable().install(source.get(0));
    }
  }

  private boolean haveConverter(SEXP source, PrimitiveMethod.Argument formal) {
    return getConverter(source,formal) != null;
  }

  private ExpConverter getConverter(SEXP source, PrimitiveMethod.Argument formal) {
    for(ExpConverter converter : converters) {
      if(converter.accept(source, formal)) {
        return converter;
      }
    }
    return null;
  }


}
