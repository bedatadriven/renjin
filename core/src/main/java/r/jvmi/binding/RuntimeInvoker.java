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

package r.jvmi.binding;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import r.base.Calls;
import r.base.ClosureDispatcher;
import r.base.dispatch.DispatchChain;
import r.lang.*;
import r.lang.exception.EvalException;

import java.lang.Boolean;
import java.lang.Class;
import java.lang.Double;
import java.lang.Integer;
import java.lang.Iterable;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.UnsupportedOperationException;
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


  private List<ArgConverter> converters;

  private RuntimeInvoker() {

    // converters between whole expressions and
    // argument types
    converters = new ArrayList<ArgConverter>();
    converters.add(new IdentityConverter());
    converters.add(new ToPrimitive());
    converters.add(new StringToSymbol());
    converters.add(new FromExternalPtr());
    converters.add(new DoubleToIndices());
    converters.add(new DoubleToInt());
    converters.add(new IntToIndices());
    converters.add(new NullToObject());
    converters.add(new EmptyVectorToRecycleablePrimitive());
  }

  public EvalResult invoke(Context context, Environment rho, Iterable<SEXP> arguments, List<JvmMethod> overloads) {

    List<ProvidedArgument> provided = Lists.newArrayList();
    for(SEXP argument : arguments) {
      provided.add(new ProvidedArgument(context, rho, argument));
    }

    return matchAndInvoke(context, rho, overloads, provided);

  }

  public EvalResult invoke(Context context, Environment rho, String name, FunctionCall call, List<JvmMethod> overloads) {

    // first check for a method which can handle the call in its entirety
    if(overloads.size() == 1 && overloads.get(0).acceptsCall()) {
      return overloads.get(0).invokeAndWrap(context, rho, call);
    }

    // make a list of the provided arguments
    List<ProvidedArgument> provided = Lists.newArrayList();
    for(PairList.Node arg : call.getArguments().nodes()) {
      if(Symbols.ELLIPSES.equals(arg.getValue())) {
        // the values of the '...' are just merged into the argument list
        DotExp ellipses = (DotExp) arg.getValue().evalToExp(context, rho);
        for(PairList.Node dotArg : ellipses.getPromises().nodes()) {
          provided.add(new ProvidedArgument(context, rho, dotArg));
        }
      } else {
        provided.add(new ProvidedArgument(context, rho, arg));
      }
    }

    // if generic, try to dispatch it to a closure in scope
    // e.g. a vector with class 'flarb' would be dispatched to 'dim.flarb' if it exists.
    EvalResult result = tryDispatchGeneric(context, rho, name, call, overloads, provided);
    if(result != null) {
      return result;
    }

    // do we have a single method that accepts the whole argument list?
    if(overloads.size() == 1 && overloads.get(0).acceptsArgumentList()) {

      return overloads.get(0).invokeWithContextAndWrap(context, rho, toEvaluatedList(overloads.get(0), provided));
    }

    return matchAndInvoke(context, rho, overloads, provided);
  }

  private EvalResult tryDispatchGeneric(Context context, Environment rho, String name, FunctionCall call,
                                        List<JvmMethod> overloads, List<ProvidedArgument> provided) {
    // check first to see if this method should be executed generically.
    if(!isGeneric(overloads)) {
      return null;
    }
    
    if(overloads.get(0).isGroupGeneric()) {
      
      PairList evaluated = Calls.evaluateList(context, rho, call.getArguments());

      EvalResult dispatched = Calls.DispatchGroup("Ops",call, name, evaluated, context, rho);
      if(dispatched != null) {
        return dispatched;
      }

    }
    
    if(call.getFunction() instanceof Symbol) {
      if(((Symbol) call.getFunction()).getPrintName().endsWith(".default")) {
        return null;
      }
    }
    if(provided.isEmpty()) {
      throw new EvalException("hmm generic primitive with zero args: " + overloads.toString());
    }

    Vector classes = (Vector)provided.get(0).evaluated().getAttribute(Symbols.CLASS);
    if(classes.length() == 0) {
      return null;
    }

    DispatchChain chain = DispatchChain.newChain(rho, overloads.get(0).getGenericName(), classes);
    if(chain == null) {
      return null;
    }

    // Repromise ?
    PairList.Node restOfArguments = (PairList.Node) call.getArguments();
    PairList newArgs = new PairList.Node(new Promise(provided.get(0).provided, provided.get(0).evaluated()),
        restOfArguments.hasNextNode() ? restOfArguments.getNextNode() : Null.INSTANCE);

    FunctionCall newCall = FunctionCall.newCall(chain.getMethodSymbol(), newArgs);

    ClosureDispatcher dispatcher = new ClosureDispatcher(context, rho, newCall);
    return dispatcher.apply(chain, newArgs);
  }

  private boolean isGeneric(List<JvmMethod> overloads) {
    boolean isGeneric = false;
    boolean isNotGeneric = false;
    for(JvmMethod overload : overloads) {
      if(overload.isGeneric()) {
        isGeneric = true;
      } else {
        isNotGeneric = true;
      }
    }
    if(isGeneric && isNotGeneric) {
      throw new EvalException("all overloads must be marked with @Generic / @GroupGeneric or none: " + overloads.toString());
    }

    return isGeneric;
  }


  public EvalResult invoke(Context context, Environment rho, FunctionCall call, PairList evaluatedArgs, List<JvmMethod> overloads) {

    // make a list of the provided arguments
    List<ProvidedArgument> provided = Lists.newArrayList();
    for(PairList.Node arg : evaluatedArgs.nodes()) {
      provided.add(new ProvidedArgument(arg));
    }

    // do we have a single method that accepts the whole argument list?
    if(overloads.size() == 1 && overloads.get(0).acceptsArgumentList()) {
      return overloads.get(0).invokeWithContextAndWrap(context, rho, toEvaluatedList(overloads.get(0), provided));
    }

    return matchAndInvoke(context, rho, overloads, provided);
  }

  private EvalResult matchAndInvoke(Context context, Environment rho, List<JvmMethod> overloads, List<ProvidedArgument> provided) {
    for(JvmMethod method : overloads) {
      if(acceptArguments(provided, method.getFormals())) {
        return invokeOverload(method, context, rho, provided);
      }
    }

    throw new EvalException(formatNoMatchingOverloadMessage(provided, overloads));
  }

  private EvalResult invokeOverload(JvmMethod method, Context context, Environment rho, List<ProvidedArgument> providedArgs) {
    SEXP[] preparedArguments = prepareArguments(method, providedArgs);
    Object[] arguments = new Object[providedArgs.size()];

    if(method.isRecycle()) {
      int cycles = countCycles(method, preparedArguments);
      BuilderAdapter result = builderFor(method.getReturnType(), cycles);
      for(int i=0;i!=cycles;++i) {
        if(convertArguments(method, preparedArguments, arguments, i)) {
          result.set( i , method.invokeWithContext(context, rho, arguments) );
        } else {
          result.setNA( i );
        }
      }

      // for unary and binary primitives with recycling, we copy some attributes from the longest element
      if(method.getFormals().size() <= 2)  {
        SEXP attributeSource = longestRecycledElement(method, providedArgs);
        result.copyAttribute(attributeSource, Symbols.DIM);
        result.copyAttribute(attributeSource, Symbols.DIMNAMES);
        result.copyAttribute(attributeSource, Symbols.NAMES);
      }

      return new EvalResult( result.build() );

    } else {
      convertArguments(method, preparedArguments, arguments, 0);
      return method.invokeWithContextAndWrap(context, rho, arguments);
    }
  }

  private SEXP longestRecycledElement(JvmMethod method, List<ProvidedArgument> arguments) {
    SEXP longest = Null.INSTANCE;
    for(int i=0;i!=arguments.size();++i) {
      if(method.getFormals().get(i).isRecycle()) {
        SEXP argument = arguments.get(i).evaluated();
        if(argument.length() > longest.length()) {
          longest = argument;
        }
      }
    }
    return longest;
  }

  private int countCycles(JvmMethod method, SEXP[] arguments) {
    int maxLength = 0;
    for(int i=0;i!=arguments.length;++i) {
      if(method.getFormals().get(i).isRecycle()) {
        if(arguments[i].length() == 0) {
          return 0;
        }
        maxLength = Math.max(maxLength, arguments[i].length());
      }
    }
    return maxLength;
  }

  private boolean convertArguments(JvmMethod method, SEXP[] preparedArgs, Object[] args, int cycle) {
    for(int i=0;i!=preparedArgs.length;++i) {
      JvmMethod.Argument formal = method.getFormals().get(i);
      if(formal.isRecycle()) {
        AtomicAccessor vector = AtomicAccessors.create(preparedArgs[i], formal.getClazz());
        int vectorIndex = cycle % vector.length();
        if(vector.isNA(vectorIndex) && !method.acceptsNA()) {
          return false;
        }
        args[i] = vector.get(vectorIndex);
      } else {
        args[i] = getConverter(preparedArgs[i], formal).convert(preparedArgs[i], formal);
      }
    }
    return true;
  }


  private Object[] toEvaluatedList(JvmMethod method, List<ProvidedArgument> arguments) {
    Object params[] = new Object[method.getFormals().size()];
    String names[] = new String[method.getFormals().size() - 1];

    // set default values for the NamedFlags
    for(int i=0;i<names.length;++i) {
      JvmMethod.Argument formal = method.getFormals().get(i + 1);
      names[i] = formal.getName();
      params[i+1] = formal.getDefaultValue();
      Preconditions.checkNotNull(names[i], "any formal argument following @ArgumentList must be annotated with @NamedFlag:\n"+ method.toString());
    }

    ListVector.Builder result = ListVector.newBuilder();
    for(ProvidedArgument arg : arguments) {
      if(arg.getTag() == Null.INSTANCE) {
        result.add(arg.evaluated());
      } else {
        String name = arg.getTagName();
        // is this a named flag?
        int index = method.getFormalIndexByName(name);
        if(index == -1) {
          result.add(name, arg.evaluated());
        } else {
          params[index] = (arg.evaluated().asReal() != 0);
        }
      }
    }
    params[0] = result.build();

    return params;
  }



  private static BuilderAdapter builderFor(Class type, int length) {
    if(type == Boolean.TYPE) {
      return new BooleanBuilder(length);
    } else if(type == Logical.class) {
      return new LogicalBuilder(length);
    } else if(type == Integer.TYPE) {
      return new IntegerBuilder(length);
    } else if(type == Double.TYPE) {
      return new DoubleBuilder(length);
    } else if(type == String.class) {
      return new StringAdapter(length);
    } else if(SEXP.class.isAssignableFrom(type)) {
      return new ListBuilder(length);
    } else {
      throw new UnsupportedOperationException("No Vector.Builder for " + type.getName() );
    }
  }


  private SEXP[] prepareArguments(JvmMethod method, List<ProvidedArgument> providedArgs) {
    SEXP newArray[] = new SEXP[providedArgs.size()];
    for(int i=0;i!=providedArgs.size();++i) {
      JvmMethod.Argument formal = method.getFormals().get(i);
      ProvidedArgument provided = providedArgs.get(i);

      newArray[i] = provided.prepare(formal);
    }
    return newArray;
  }

  private boolean acceptArguments(List<ProvidedArgument> provided, List<JvmMethod.Argument> formals) {
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

  private static String formatNoMatchingOverloadMessage(List<ProvidedArgument> provided, List<JvmMethod> methods) {
    StringBuilder sb = new StringBuilder();
    sb.append("Cannot execute the function with the arguments supplied.\n");
    appendProvidedArguments(sb, provided);

    sb.append("\nAvailable overloads (in ").append(methods.get(0).getDeclaringClass().getName()).append(") :\n");

    appendOverloadsTo(methods, sb);

    return sb.toString();
  }

  private static void appendProvidedArguments(StringBuilder sb, List<ProvidedArgument> provided) {
    sb.append("Arguments: \n\t");
    for(ProvidedArgument arg : provided) {
      sb.append(arg.getTypeName()).append(" ");
    }
  }

  private static void appendOverloadsTo(List<JvmMethod> methods, StringBuilder sb) {
    for(JvmMethod method : methods) {
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
    private Context context;

    private ProvidedArgument(PairList.Node evaluatedArg) {
      this.tag = evaluatedArg.getRawTag();
      this.provided = this.evaluated = evaluatedArg.getValue();
    }


    public ProvidedArgument(Context context, Environment rho, PairList.Node arg) {
      this.context = context;
      this.rho = rho;
      this.provided = arg.getValue();
      this.tag = arg.getRawTag();
    }

    public ProvidedArgument(Context context, Environment rho, SEXP argument) {
      this.context = context;
      this.rho = rho;
      this.provided = argument;
      this.tag = Null.INSTANCE;
    }


    public boolean canBePassedTo(JvmMethod.Argument formal) {
      if(formal.hasExplicitConverter()) {
        // we have to assume that the R converter function (ex: as.character)
        // will convert the argument appropriately or throw its own error.
        return true;
      } else if(formal.isEvaluated()) {
        return canBePassedTo(evaluated(), formal);
      } else {
        return canBePassedTo(provided, formal);
      }
    }

    private SEXP evaluated() {
      if(evaluated == null ) {
        if(provided == Symbol.MISSING_ARG) {
          evaluated = Symbol.MISSING_ARG;
        } else {         
          evaluated = provided.evaluate(context, rho).getExpression();
          if(evaluated instanceof Promise) {
            evaluated = evaluated.evalToExp(context, rho);
          }
        }
      }
      return evaluated;
    }

    private boolean canBePassedTo(SEXP provided, JvmMethod.Argument formal) {
      if(formal.isAssignableFrom(provided)) {
        return true;
      } else {
        return haveConverter(provided, formal);
      }
    }

    public SEXP prepare(JvmMethod.Argument formal) {
      SEXP value;
      if(formal.hasExplicitConverter()) {
        // call into the R function responsable for convertering this argument
        value = formal.getExplicitConverter().convert(context, rho, provided);
      } else if(formal.isEvaluated()) {
        value = evaluated();
      } else {
        value = provided;
      }
      return value;
    }

    public String getTypeName() {
      try {
        return evaluated().getTypeName();
      } catch(EvalException e) {
        return provided.getTypeName();
      }
    }

    public SEXP getTag() {
      return tag;
    }

    public String getTagName() {
      return ((Symbol)tag).getPrintName();
    }
  }

  private interface ArgConverter<S extends SEXP, T> {
    boolean accept(SEXP source, JvmMethod.Argument formal);
    T convert(S source, JvmMethod.Argument formal);
  }

  private class ToPrimitive implements ArgConverter {

    @Override
    public boolean accept(SEXP source, JvmMethod.Argument formal) {
      return AtomicAccessors.haveAccessor(source, formal.getClazz()) && (
          (source.length() == 1 || formal.isRecycle()));
    }

    @Override
    public Object convert(SEXP source, JvmMethod.Argument formal) {
      return AtomicAccessors.create(source, formal.getClazz()).get(0);
    }
  }

  private class EmptyVectorToRecycleablePrimitive implements ArgConverter {

    @Override
    public boolean accept(SEXP source, JvmMethod.Argument formal) {
      return source instanceof Vector && source.length() == 0 && formal.isRecycle();
    }

    @Override
    public Object convert(SEXP source, JvmMethod.Argument formal) {
      throw new UnsupportedOperationException("should not be called");
    }
  }

  private class StringToSymbol implements ArgConverter<StringVector, Symbol> {

    @Override
    public boolean accept(SEXP source, JvmMethod.Argument formal) {
      return source instanceof StringVector &&
          source.length() == 1 &&
          formal.isSymbol();
    }

    @Override
    public Symbol convert(StringVector source, JvmMethod.Argument formal) {
      return Symbol.get(source.getElement(0));
    }
  }

  private class DoubleToIndices implements ArgConverter<DoubleVector, int[]> {
    @Override
    public boolean accept(SEXP source, JvmMethod.Argument formal) {
      return
          source instanceof DoubleVector &&
              formal.getClazz().equals(int[].class);
    }

    @Override
    public int[] convert(DoubleVector source, JvmMethod.Argument formal) {
      return source.coerceToIntArray();
    }
  }

  private class IntToIndices implements ArgConverter<IntVector,  int[]> {
    @Override
    public boolean accept(SEXP source, JvmMethod.Argument formal) {
      return
          source instanceof IntVector &&
              formal.getClazz().equals(int[].class);
    }

    @Override
    public int[] convert(IntVector source, JvmMethod.Argument formal) {
      return source.toIntArray();
    }
  }

  /**
   * As a special case, double scalars can be passed with as an integer implicitly
   * when the argument has the meaning of an index
   */
  private class DoubleToInt implements ArgConverter<DoubleVector, Integer> {
    @Override
    public boolean accept(SEXP source, JvmMethod.Argument formal) {
      return source instanceof DoubleVector &&
          source.length() == 1 &&
          formal.getClazz().equals(Integer.TYPE);
    }

    @Override
    public Integer convert(DoubleVector source, JvmMethod.Argument formal) {
      return (int)source.get(0);
    }
  }

  private boolean haveConverter(SEXP source, JvmMethod.Argument formal) {
    return getConverter(source,formal) != null;
  }

  private ArgConverter getConverter(SEXP source, JvmMethod.Argument formal) {
    for(ArgConverter converter : converters) {
      if(converter.accept(source, formal)) {
        return converter;
      }
    }
    return null;
  }


  private class FromExternalPtr implements ArgConverter {

    @Override
    public boolean accept(SEXP source, JvmMethod.Argument formal) {
      return source instanceof ExternalExp && formal.getClazz().isAssignableFrom(
          ((ExternalExp) source).getValue().getClass());
    }

    @Override
    public Object convert(SEXP source, JvmMethod.Argument formal) {
      return ((ExternalExp)source).getValue();
    }
  }

  // allow R's NULL to be converted to a null java argument for
  // non-R classes
  private class NullToObject implements ArgConverter {
    @Override
    public boolean accept(SEXP source, JvmMethod.Argument formal) {
      return source == Null.INSTANCE && !formal.getClazz().isPrimitive() &&
          !SEXP.class.isAssignableFrom(formal.getClazz());
    }

    @Override
    public Object convert(SEXP source, JvmMethod.Argument formal) {
      return null;
    }
  }

  private class IdentityConverter implements ArgConverter {
    @Override
    public boolean accept(SEXP source, JvmMethod.Argument formal) {
      return formal.isAssignableFrom(source);
    }

    @Override
    public Object convert(SEXP source, JvmMethod.Argument formal) {
      return source;
    }
  }

  private static abstract class BuilderAdapter<B extends Vector.Builder, E> {
    protected final B builder;

    protected BuilderAdapter(B builder) {
      this.builder = builder;
    }

    public void setNA(int index) {
      builder.setNA(index);
    }

    public abstract void set(int index, E value);

    public SEXP build() {
      return builder.build();
    }

    public void copyAttribute(SEXP attributeSource, String name) {
      builder.setAttribute(name, attributeSource.getAttribute(Symbol.get(name)));
    }
    
    public void copyAttribute(SEXP attributeSource, Symbol name) {
      builder.setAttribute(name, attributeSource.getAttribute(name));      
    }
  }

  private static class BooleanBuilder extends BuilderAdapter<LogicalVector.Builder, Boolean> {
    private BooleanBuilder(int length) {
      super(new LogicalVector.Builder(length));
    }

    @Override
    public void set(int index, Boolean value) {
      builder.set(index, value);
    }
  }

  private static class LogicalBuilder extends BuilderAdapter<LogicalVector.Builder, Logical> {
    private LogicalBuilder(int length) {
      super(new LogicalVector.Builder(length));
    }

    @Override
    public void set(int index, Logical value) {
      builder.set(index, value);
    }
  }


  private static class IntegerBuilder extends BuilderAdapter<IntVector.Builder, Integer> {

    public IntegerBuilder(int length) {
      super(new IntVector.Builder(length));
    }
    @Override
    public void set(int index, Integer value) {
      builder.set(index, value);
    }
  }

  private static class DoubleBuilder extends BuilderAdapter<DoubleVector.Builder, Double> {
    public DoubleBuilder(int length) {
      super(new DoubleVector.Builder(length));
    }

    @Override
    public void set(int index, Double value) {
      builder.set(index, value);
    }
  }

  private static class StringAdapter extends BuilderAdapter<StringVector.Builder, String> {
    public StringAdapter(int length) {
      super(new StringVector.Builder(length));
    }

    @Override
    public void set(int index, String value) {
      builder.set(index, value);
    }
  }

  private static class ListBuilder extends BuilderAdapter<ListVector.Builder, SEXP> {
    public ListBuilder(int length) {
      super(new ListVector.Builder(length));
    }

    @Override
    public void set(int index, SEXP value) {
      builder.set(index, value);
    }
  }

}
