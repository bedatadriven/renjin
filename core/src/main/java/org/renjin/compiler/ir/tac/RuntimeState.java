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
package org.renjin.compiler.ir.tac;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.NamedShape;
import org.renjin.compiler.ir.Shape;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.eval.Context;
import org.renjin.packaging.SerializedPromise;
import org.renjin.primitives.Evaluation;
import org.renjin.primitives.S3;
import org.renjin.primitives.sequence.DoubleSequence;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides access to the runtime environment at the moment of compilation,
 * recording all lookups as assumptions that can be tested at future moments
 * in the runtime.
 */
public class RuntimeState {
  private Context context;
  private Environment rho;
  
  private Environment methodTable;

  private List<RuntimeAssumption> assumptions = new ArrayList<>();


  /**
   * List of symbols that we have resolved to builtins / or inlined
   * closures. We need to check at the end that there is no possiblity
   * they have been assigned to.
   */
  private Map<Symbol, Function> resolvedFunctions = Maps.newHashMap();
  private List<ExtraArgument> extraArguments;


  /**
   * Creates a new {@code RuntimeState} for an arbitrary execution environment.
   * @param context
   * @param rho
   */
  public RuntimeState(Context context, Environment rho) {
    this.context = context;
    this.rho = rho;
  }

  public int getNumArgs() {
    int numArgs = Evaluation.nargs(context, rho);
    assumptions.add(new RuntimeAssumption() {
      @Override
      public boolean test(Context context, Environment rho) {
        return numArgs == Evaluation.nargs(context, rho);
      }
    });
    return numArgs;
  }

  /**
   * Creates a new {@code RuntimeState} for an inlined function.
   * @param parentState
   * @param enclosingEnvironment
   */
  public RuntimeState(RuntimeState parentState, Environment enclosingEnvironment) {
    this(parentState.context, enclosingEnvironment);
    
    SEXP methodTableSexp = enclosingEnvironment.getVariable(S3.METHODS_TABLE);
    if(methodTableSexp instanceof Promise) {
      throw new NotCompilableException(S3.METHODS_TABLE, S3.METHODS_TABLE + " is not evaluated.");
    }
    if(methodTableSexp instanceof Environment) {
      methodTable = (Environment) methodTableSexp;
    }
  }

  public List<ExtraArgument> findEllipses() {
    if(extraArguments != null) {
      return extraArguments;
    }
    SEXP ellipses = rho.getEllipsesVariable();
    if(ellipses == Symbol.UNBOUND_VALUE) {
      throw new InvalidSyntaxException("'...' used in an incorrect context.");
    }
    extraArguments = new ArrayList<>();
    if(ellipses instanceof PairList) {
      PairList pairList = (PairList) ellipses;
      for (PairList.Node node : pairList.nodes()) {
        SEXP value = forceWithoutSideEffects(context, false, node.getValue());
        if(value instanceof Promise) {
          throw new NotCompilableException(node.getValue(), "Unevaluated promise encountered");
        }
        if (node.hasName()) {
          extraArguments.add(new ExtraArgument(node.getName(), reasonableBounds(value)));
        } else {
          extraArguments.add(new ExtraArgument(reasonableBounds(value)));
        }
      }
    }
    assumptions.add(new AssumeEllipses(extraArguments));
    return extraArguments;
  }

  /**
   * Finds the value bounds of a variable that is coming from "outside" the
   * compiled S-expression. The bounds returned are recorded as assumptions for
   * future runs.
   *
   */
  public Optional<ValueBounds> findVariableBounds(Symbol name) {

    SEXP value = findVariableWithoutSideEffects(context, rho, name, false);
    if(value == Symbol.UNBOUND_VALUE) {
      return Optional.empty();
    }

    ValueBounds bounds = reasonableBounds(value);

    assumptions.add(new AssumeVariableBounds(name, bounds));

    return Optional.of(bounds);
  }


  /**
   * Climbs the enclosing environment tree to find a variable, but *without* triggering
   * any side effects in the form of active bindings or unevaluated promises.
   *
   * @param rho the enclosing environment
   * @param name the name of the binding to find
   * @param findFunction true if we are looking for a function.
   * @return the value of the binding, or {@code Symbol.UNBOUND_VALUE} if it could not be found.
   * @throws NotCompilableException if an active binding or unevaluated promise was encountered in the search path.
   */
  private static SEXP findVariableWithoutSideEffects(Context context, Environment rho, Symbol name, boolean findFunction) {

    SEXP value;
    Environment environment = rho;

    do {
      if (environment.isActiveBinding(name)) {
        throw new NotCompilableException(name, "Active Binding encountered");
      }

      // Using unsafe because we already checked for active binding
      value = environment.getVariableUnsafe(name);

      // Functions loaded from packages are initialized stored as SerializedPromises
      // so the AST does not have to be loaded into memory until the function is first
      // needed. Though technically this does involve I/O and thus side-effects,
      // we don't consider it to have any affect on the running program, and so
      // can safely force it.

      if(value instanceof SerializedPromise) {
        value = value.force(context);
      }

      value = forceWithoutSideEffects(context, findFunction, value);
      if(value instanceof Promise) {
        throw new NotCompilableException(name, "Unevaluated promise encountered");
      }

      if(value != Symbol.UNBOUND_VALUE) {
        if(!findFunction || value instanceof Function) {
          break;
        }
      }

      // Climb up to the next level
      environment = environment.getParent();

    } while(environment != Environment.EMPTY);

    return value;
  }

  private static SEXP forceWithoutSideEffects(Context context, boolean findFunction, SEXP value) {
    while(value instanceof Promise) {
      Promise promisedValue = (Promise) value;

      if(promisedValue.isEvaluated()) {
        value = promisedValue.force(context);

      } else if(promisedValue.getExpression() instanceof Symbol) {
        // For simple expressions like a Symbol, we may still be able to resolve
        // the expression without side effects....
        value = findVariableWithoutSideEffects(context,
            promisedValue.getEnvironment(),
            (Symbol) promisedValue.getExpression(), findFunction);

      } else if(!hasSideEffects(promisedValue.getExpression())) {
        value = promisedValue.force(context);

      } else {
        // Promises can have side effects, and evaluation order is important
        // so we can't just force all the promises in the beginning of the loop
        break;
      }
    }
    return value;
  }

  private static boolean hasSideEffects(SEXP sexp) {
    if(sexp instanceof AtomicVector) {
      return false;
    }
    if(sexp instanceof ListVector) {
      return false;
    }
    return true;
  }

  /**
   * Now decide how much we want to assume about this value.
   * <p>
   * The more information we include in the value bounds, the more we will be
   * able to specialize and the more efficient our result will be.
   * <p>
   * The more we surface as a value bounds, however, the less likely we will be able
   * to reuse the compiled fragment.
   * <p>
   * So we need a series of heuristics to determine what is useful to assume
   * and what we should leave open
   *
   */
  public static ValueBounds reasonableBounds(SEXP sexp) {
    return reasonableBounds(sexp, 0);
  }

  /**
   * Now decide how much we want to assume about this value.
   * <p>
   * The more information we include in the value bounds, the more we will be
   * able to specialize and the more efficient our result will be.
   * <p>
   * The more we surface as a value bounds, however, the less likely we will be able
   * to reuse the compiled fragment.
   * <p>
   * So we need a series of heuristics to determine what is useful to assume
   * and what we should leave open
   *
   */
  private static ValueBounds reasonableBounds(SEXP sexp, int depth) {

    int length = sexp.length();
    int type = TypeSet.of(sexp);

    // 1) Constants
    // These types of S-Expressions are unlikely to change across executions, and
    // have a huge impact on specialization.

    if (length == 0 || type == TypeSet.SYMBOL || type == TypeSet.NULL || type == TypeSet.FUNCTION) {
      return ValueBounds.constantValue(sexp);
    }

    // 2) Numeric, scalar of values 0 and 1 we will treat as constants
    if(length == 1 && (type == TypeSet.DOUBLE || type == TypeSet.INT || type == TypeSet.LOGICAL)) {
      double doubleValue = ((AtomicVector) sexp).getElementAsDouble(0);
      if(doubleValue == 0d || doubleValue == 1d || DoubleVector.isNA(doubleValue)) {
        return ValueBounds.constantValue(sexp);
      }
    }

    // 3) For vectors, the presence/absence of attributes are essential for specialization

    ValueBounds.Builder bounds = ValueBounds.builder()
        .setTypeSet(type)
        .setAttributes(sexp.getAttributes());


    // 3a) List vectors are used for many structured data types, record the general shape,
    // with some limits to avoid going overboard

    if(sexp instanceof ListVector) {
      if(depth < 3) {
        bounds.setShape(reasonableShape((ListVector) sexp, depth));
      }
    }

    // 4) Atomic vectors we want to assume their type and some properties about their
    //    values that are required (or useful) for specialization
    if(sexp instanceof AtomicVector) {
      Vector vector = (Vector) sexp;

      if (length == 1 && vector.isElementNA(0)) {
        return ValueBounds.constantValue(sexp);
      }

      /// Treat scalars specially, this is hugely important to specialization
      if (length == 1) {

        if (vector.isElementNA(0)) {
          // Scalar NAs assume constant
          return ValueBounds.constantValue(sexp);

        } else {
          bounds.addFlags(ValueBounds.FLAG_NO_NA | ValueBounds.LENGTH_ONE);
          bounds.addFlags(ValueBounds.FLAG_POSITIVE, vector.getElementAsDouble(0) > 0);
        }

      } else {
        // We don't want to spend a lot of time checking for NAs, but if it is trivial to
        // determine, then stel that vast.
        bounds.addFlags(ValueBounds.FLAG_NO_NA,
            vector instanceof IntSequence ||
                vector instanceof DoubleSequence);
      }
    }


    return bounds.build();
  }

  private static Shape reasonableShape(ListVector list, int depth) {
    int n = list.length();
    if(n < 30) {
      ValueBounds elements[] = new ValueBounds[n];
      for (int i = 0; i < n; i++) {
        elements[i] = reasonableBounds(list.getElementAsSEXP(i), depth + 1);
      }
      return new NamedShape(list.getNames(), elements);
    }
    return null;
  }

  /**
   * Finds a function with the given name in the enclosing environment, without
   * triggering any side affects.
   *
   * @param functionName the name of the function to lookup
   */
  public Function findFunction(Symbol functionName) {

    Function f = findFunctionIfExists(functionName);
    if (f != null) {
      assumptions.add(new AssumeFunctionDefinition(functionName, f));
      return f;
    }
    throw new NotCompilableException(functionName, "Could not find function " + functionName);
  }

  public Function findNamespaceExport(Symbol namespace, Symbol export) {
    SEXP value = context.getNamespaceRegistry().getNamespace(context, namespace).getEntry(export).force(context);
    if(value instanceof Function) {
      return (Function) value;
    }
    // TODO: Record assumption
    throw new NotCompilableException(FunctionCall.newCall(Symbol.get("::"), namespace, export), "Not a function");
  }

  private Function findFunctionIfExists(Symbol functionName) {
    if(resolvedFunctions.containsKey(functionName)) {
      return resolvedFunctions.get(functionName);
    }

    SEXP function = findVariableWithoutSideEffects(context, rho, functionName, true);
    if(function == Symbol.UNBOUND_VALUE) {
      return null;
    } else {
      resolvedFunctions.put(functionName, (Function) function);
      return (Function) function;
    }
  }

  public Map<Symbol, Function> getResolvedFunctions() {
    return resolvedFunctions;
  }

  /**
   * Tries to safely resolve an S3 method, without forcing any promises that might have side effects.
   * @param generic
   * @param group
   * @param objectClasses
   */
  public Function findMethod(String generic, String group, StringVector objectClasses) {

    Function method;
    
    for(String className : objectClasses) {
      method = findMethod(generic, group, className);
      if(method != null) {
        return method;
      }
    }
    
    return findMethod(generic, group, "default");
  }
  
  private Function findMethod(String generic, String group, String className) {
    
    Function method = findMethod(generic, className);
    if(method != null) {
      return method;
    }
    if(group != null) {
      method = findMethod(group, className);
      if(method != null) {
        return method;
      }
    }
    return null;
  }

  private Function findMethod(String generic, String className) {

    // TODO: this requires some thought because we are making
    // assumptions about not only the functions we find, but those we DON'T find,
    // and method table entries can easily be changed if new packages are loaded, even if the
    // environment is sealed.

    Symbol method = Symbol.get(generic + "." + className);
    Function function = findFunctionIfExists(method);
    if(function != null) {
      return function;
    }
  
    if(methodTable != null) {
      SEXP functionSexp = methodTable.getVariable(method);
      if(functionSexp instanceof Promise) {
        throw new NotCompilableException(method, "Unevaluated entry in " + S3.METHODS_TABLE);
      }
      if(functionSexp instanceof Function) {
        return (Function) functionSexp;
      }
    }
    
    return null;
  }

  public List<RuntimeAssumption> getAssumptions() {
    return assumptions;
  }

  public boolean isMissing(Symbol name) {
    throw new UnsupportedOperationException("TODO");
  }



  /**
   * Assumes that the variable in the expression's immediate environment
   * matches the ValueBounds used to compile the fragment
   */
  public static class AssumeVariableBounds implements RuntimeAssumption {
    private final Symbol name;
    private final ValueBounds bounds;

    public AssumeVariableBounds(Symbol name, ValueBounds bounds) {
      this.name = name;
      this.bounds = bounds;
    }

    @Override
    public String toString() {
      return "Variable{" + name + " = " + bounds + "}";
    }

    @Override
    public boolean test(Context context, Environment rho) {
      SEXP value;
      try {
        value = findVariableWithoutSideEffects(context, rho, name, false);
      } catch (NotCompilableException e) {
        return false;
      }
      if(value == Symbol.UNBOUND_VALUE) {
        return false;
      }
      return bounds.test(value);
    }
  }

  public static class AssumeFunctionDefinition implements RuntimeAssumption {
    private final Symbol name;
    private final Function function;

    public AssumeFunctionDefinition(Symbol name, Function function) {
      this.name = name;
      this.function = function;
    }

    @Override
    public String toString() {
      if (function instanceof PrimitiveFunction) {
        return "Function{" + name + " = " + ((PrimitiveFunction) function).getName() + "()}";
      } else {
        return "Function{" + name + " = f() }";
      }
    }

    @Override
    public boolean test(Context context, Environment rho) {
      SEXP value;
      try {
        value = findVariableWithoutSideEffects(context, rho, name, false);
      } catch (NotCompilableException e) {
        return false;
      }
      if(value == Symbol.UNBOUND_VALUE) {
        return false;
      }

      // TODO: This will not allow us to reuse compiled fragments between
      // different Renjin sessions
      return function == value;
    }
  }

  public static class AssumeEllipses implements RuntimeAssumption {

    private final List<ExtraArgument> arguments;

    public AssumeEllipses(List<ExtraArgument> arguments) {
      this.arguments = arguments;
    }

    @Override
    public boolean test(Context context, Environment rho) {
      SEXP ellipses = rho.getEllipsesVariable();
      if(arguments.isEmpty() && ellipses == Null.INSTANCE) {
        return true;
      }
      if(!(ellipses instanceof PairList)) {
        return false;
      }
      for (ExtraArgument expectedArgument : arguments) {
        if (!(ellipses instanceof PairList.Node)) {
          return false;
        }
        PairList.Node argument = (PairList.Node) ellipses;
        if(!argument.getName().equals(expectedArgument.getName())) {
          return false;
        }
        if(!expectedArgument.getBounds().test(argument.getValue())) {
          return false;
        }
        ellipses = argument.getNext();
      }
      if(ellipses != Null.INSTANCE) {
        // Are there more 'extra arguments' than expected?
        return false;
      }
      return true;
    }
  }

}