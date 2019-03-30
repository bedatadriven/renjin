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
package org.renjin.sexp;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.repackaged.guava.collect.UnmodifiableIterator;

import java.util.*;
import java.util.function.Predicate;

/**
 * The Environment data type.
 *
 * <p>
 * Environments can be thought of as consisting of two things:
 * <ul>
 * <li>A <strong>frame</strong>, consisting of a set of symbol-value pairs, and
 * <li>an enclosure, a pointer to an enclosing environment.</li>
 * </ul>
 *
 * <p>
 * When R looks up the vbalue for a symbol the frame is examined and if a
 * matching symbol is found its value will be returned. If not, the enclosing environment
 *  is then accessed and the process repeated.
 * Environments form a tree structure in which the enclosures play the role of parents.
 *  The tree of environments is rooted in an empty environment,
 * available through emptyenv(), which has no parent.
 * It is the direct parent of the environment of the base package
 * (available through the baseenv() function). Formerly baseenv() 
 * had the special value {@code NULL}, but as from version 2.4.0, the
 *  use of {@code NULL} as an environment is defunct.
 *
 */
public abstract class Environment extends AbstractSEXP implements Recursive {


  public static final String TYPE_NAME = "environment";
  public static final String GLOBAL_ENVIRONMENT_NAME = "R_GlobalEnv";
  public static final String BASE_NAMESPACE_ENVIRONMENT = "namespace:base";
  public static final String BASE_ENVIRONMENT = "base";

  private String name = null;
  private Environment parent;

  private boolean locked;
  private Set<Symbol> lockedBindings;
  private Map<Symbol, Closure> activeBindings = null;

  /**
   * The root of the environment hierarchy.
   */
  public static final EmptyEnv EMPTY = new EmptyEnv();


  public static Environment createChildEnvironment(Environment parent) {
    return createChildEnvironment(parent, new HashFrame());
  }

  public static Environment createNamespaceEnvironment(Environment parent, String namespaceName) {
    return new DynamicEnvironment("namespace:" + namespaceName, parent, new HashFrame());
  }
  
  public static Environment createNamedEnvironment(Environment parent, String name) {
    return new DynamicEnvironment(name, parent, new HashFrame());
  }

  public static Environment createChildEnvironment(Environment parent, Frame frame) {
    return new DynamicEnvironment(null, parent, frame);
  }

  protected Environment(Environment parent, String name, AttributeMap attributes) {
    super(attributes);
    this.parent = parent;
    this.name = name;
  }

  public final void remove(Symbol symbol) {
    if(locked) {
      throw new EvalException("cannot remove bindings from a locked environment");
    }
    if(isActiveBinding(symbol)) {
      activeBindings.remove(symbol);
    }
    removeBinding(symbol);
  }


  public final String getName() {
    SEXP nameAttribute = this.getAttributes().get(Symbols.NAME);
    if(nameAttribute instanceof StringVector) {
      return ((StringVector) nameAttribute).getElementAsString(0);
    } else if(name == null) {
      return Integer.toString(hashCode());
    } else {
      return name;
    }
  }

  /**
   * return the parent Environment
   *
   * @return parent environment
   */
  public final Environment getParent() {
    return parent;
  }

  /**
   * set parent environment to provided environment
   *
   * @param parent environment to be set as parent environment
   */
  public final void setParent(Environment parent) {
    this.parent = parent;
  }

  public final Environment insertAbove(Frame frame) {
    Environment newEnv = new DynamicEnvironment(null, parent, frame);
    setParent(newEnv);
    return newEnv;
  }

  @Override
  public final String getTypeName() {
    return TYPE_NAME;
  }

  public final Collection<Symbol> getSymbolNames() {
    List<Symbol> ordered = new ArrayList<Symbol>(listBindings());
    if(activeBindings != null) {
      List<Symbol> ordered2 = new ArrayList<>(activeBindings.keySet());
      ordered.addAll(ordered2);
    }

    Collections.sort(ordered, (o1, o2) -> {
      if(o1.getPrintName().startsWith(".") && !o2.getPrintName().startsWith(".")){
        return 1;
      } else if(!o1.getPrintName().startsWith(".") && o2.getPrintName().startsWith(".")){
        return -1;
      } else {
        return o1.getPrintName().compareTo(o2.getPrintName());
      }
    });
    return ordered;
  }


  @Override
  public final StringVector getNames() {
    StringVector.Builder names = new StringVector.Builder();
    for (Symbol name : getSymbolNames()) {
      names.add(name.getPrintName());
    }
    return names.build();
  }

  public final boolean bindingIsLocked(Symbol symbol) {
    return lockedBindings != null && lockedBindings.contains(symbol);
  }

  /**
   * set Variable without checking if variable is an 'active' binding or if the binding is locked.
   *
   * @param symbol  the {@code SYMSXP} that should be looked up
   * @param value value to be assigned.
   */
  public final void setVariableUnsafe(Symbol symbol, SEXP value) {
    updateBinding(symbol, value);
  }

  /**
   * set Variable without checking if variable is an 'active' binding.
   * Only be used when it is absolutely certain that the Symbol is not used in any active binding.
   *
   * @param name variable name.
   * @param value value to be assigned.
   * @throws AssertionError when active bindings are present.
   */
  public final void setVariableUnsafe(String name, SEXP value) {
    if(StringVector.isNA(name)) {
      name = "NA";
    }
    setVariableUnsafe(Symbol.get(name), value);
  }

  /**
   * set Variable without context (for backward compatibility). This method does not invoke active bindings.
   * Use setVariableUnsafe instead, when variable can not be an active binding.
   *
   * @param name variable name
   * @param value value to be assigned
   */
  @Deprecated
  public final void setVariable(String name, SEXP value) {
    setVariableUnsafe(name, value);
  }

  @Deprecated
  public final void setVariable(Symbol symbol, SEXP value) {
    setVariableUnsafe(symbol, value);
  }

  /**
   * sets Variable and invokes function if variable is an active binding.
   * This is the default binding method that should be used.
   *
   * @param context the current evaluation context
   * @param name variable name.
   * @param value value to be assigned.
   * @return invisible NULL
   * @throws AssertionError when Context is not null
   */
  public final SEXP setVariable(Context context, String name, SEXP value) {
    assert ( context != null );
    if(StringVector.isNA(name)) {
      name = "NA";
    }
    return setVariable(context, Symbol.get(name), value);
  }

  /**
   * sets Variable and invokes function if variable is an active binding.
   * This is the default binding method that should be used.
   *
   *
   * @param context the current evaluation context
   * @param symbol  the {@code SYMSXP} that should be looked up
   * @param value the value/closure to be assigned
   * @return invisible NULL
   * @throws AssertionError when Context is null.
   */
  public final SEXP setVariable(Context context, Symbol symbol, SEXP value) {
    assert ( context != null );

    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("Unbound: " + symbol);
    }

    if(bindingIsLocked(symbol)) {
      throw new EvalException("cannot change value of locked binding for '%s'", symbol.getPrintName());
    }

    if(activeBindings != null && activeBindings.containsKey(symbol)) {
      Closure fun = activeBindings.get(symbol);
      PairList.Builder args = new PairList.Builder().add(value);
      return context.evaluate(new FunctionCall(fun, args.build()));
    }

    if(locked && !isBound(symbol)) {
      throw new EvalException("cannot add bindings to a locked environment");
    }

    updateBinding(symbol, value);

    return Null.INSTANCE;
  }

  /**
   * Creates an active binding to given variable name
   *
   * @param symbol variable name
   * @param closure function closure to bind to variable
   */
  public final void makeActiveBinding(Symbol symbol, Closure closure) {
    if(bindingIsLocked(symbol)) {
      throw new EvalException("cannot change value of locked binding for '%s'", symbol.getPrintName());
    } else if(locked && !isBound(symbol)) {
      throw new EvalException("cannot add bindings to a locked environment");
    }
    if(isBound(symbol)) {
      throw new EvalException("Error in makeActiveBinding(%s, %s, %s) :\n   symbol already has a regular binding",
        symbol.getPrintName(), closure.getTypeName(), closure.getEnclosingEnvironment().getTypeName());
    }
    if(activeBindings == null) {
      activeBindings = new HashMap<>();
    }
    activeBindings.put(symbol, closure);
  }

  /**
   * Checks if active bindings are assigned to a given variable name
   *
   * @param symbol variable name
   * @return
   */
  public boolean isActiveBinding(Symbol symbol) {
    return activeBindings != null && activeBindings.containsKey(symbol);
  }

  public boolean isActiveBinding(String name) {
    return isActiveBinding(Symbol.get(name));
  }

  /**
   * returns the active binding without invocation
   *
   * @param symbol name of active binding
   * @return
   */
  public Closure getActiveBinding(Symbol symbol) {
    return activeBindings.get(symbol);
  }

  /**
   * Searches the environment for a value that matches the given predicate.
   *
   *
   *
   * @param context the current evaluation context
   * @param symbol  the {@code SYMSXP} that should be looked up
   * @param predicate a predicate that tests possible return values
   * @param inherits if {@code true}, enclosing frames are searched
   * @return the bound value or {@code Symbol.UNBOUND_VALUE} if not found
   */
  public SEXP findVariable(Context context, Symbol symbol, Predicate<SEXP> predicate, boolean inherits) {
    SEXP value = getVariable(context, symbol);
    if(value != Symbol.UNBOUND_VALUE) {
      if(value instanceof Promise) {
        value = value.force(context);
      }
      if(predicate.test(value)) {
        return value;
      }
    }
    if(inherits) {
      return parent.findVariable(context, symbol, predicate, inherits);
    } else {
      return Symbol.UNBOUND_VALUE;
    }
  }

  /**
   * Recursively searches this environment and its parent for the symbol {@code symbol}. Returns the
   * binding value or in case of active binding returns the result of function evaluation in current context.
   * 
   * @param symbol the {@code SYMSXP} that should be looked up
   * @return the bound value, or {@code Symbol.UNBOUND_VALUE} if not found, or if active binding the result of
   * function evaluation in the current context
   */
  public SEXP findVariable(Context context, Symbol symbol) {
    if(symbol.isVarArgReference()) {
      return findVarArg(symbol.getVarArgReferenceIndex());
    }
    if(activeBindings != null && activeBindings.containsKey(symbol)) {
      return evaluateFunction(context, symbol);
    }
    SEXP value = getBinding(symbol);
    if(value != Symbol.UNBOUND_VALUE) {
      return value;
    }
    return parent.findVariable(context, symbol);
  }

  private SEXP evaluateFunction(Context context, Symbol symbol) {
    Closure fun = activeBindings.get(symbol);
    PairList.Builder args = new PairList.Builder();
    return context.evaluate(new FunctionCall(fun, args.build()));
  }

  /**
   * findVariable without context information (for backward compatibility), please use findVariableUnsafe()
   * instead, if variable can not be an active binding.
   * recursively searches the frames and parent frames and returns the variable but does not invoke active
   * bindings
   *
   * @param symbol the {@code SYMSXP} that should be looked up
   * @return
   */
  @Deprecated
  public final SEXP findVariable(Symbol symbol) {
    return findVariableUnsafe(symbol);
  }

  /**
   * Recursively searches this environment and its parent for the symbol {@code symbol} assuming there are no
   * active bindings present in the current environment (up to the environment where {@code symbol} is found)
   *
   * @param symbol the {@code SYMSXP} that should be looked up
   * @return
   * @throws AssertionError if the symbol is an active binding
   */
  public final SEXP findVariableUnsafe(Symbol symbol) {
    if(symbol.isVarArgReference()) {
      return findVarArg(symbol.getVarArgReferenceIndex());
    }
    assert ( !isActiveBinding(symbol) );
    SEXP value = getBinding(symbol);
    if(value != Symbol.UNBOUND_VALUE) {
      return value;
    }
    if(parent instanceof EmptyEnv) {
      return Symbol.UNBOUND_VALUE;
    }
    return parent.findVariableUnsafe(symbol);
  }

  /**
   * returns varArg value at provided index
   *
   * @param varArgReferenceIndex index of varArg to return
   * @return
   */
  public final SEXP findVarArg(int varArgReferenceIndex) {
    SEXP ellipses = findVariableUnsafe(Symbols.ELLIPSES);
    if(ellipses == Symbol.UNBOUND_VALUE) {
      throw new EvalException("..%d used in an incorrect context, no ... to look in", varArgReferenceIndex);
    }
    PairList varArgs = (PairList) ellipses;
    if(varArgs.length() < varArgReferenceIndex) {
      throw new EvalException("The ... list does not contain %d items", varArgReferenceIndex);
    }
    return varArgs.getElementAsSEXP(varArgReferenceIndex - 1);
  }

  /**
   * return variable value or invoke associated active binding and otherwise throw error
   *
   * @param context
   * @param symbol the {@code SYMSXP} that should be looked up
   * @return
   * @throws EvalException if variable is not found
   */
  public final SEXP findVariableOrThrow(Context context, Symbol symbol) {
    SEXP value = findVariable(context, symbol);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("object '" + symbol.getPrintName() + "' not found");
    }
    return value;
  }


  public Function findFunction(Context context, Symbol symbol) {
    Function value = getFunctionBinding(context, symbol);
    if(value != null) {
      return value;
    }
    return parent.findFunction(context, symbol);
  }


  /**
   *
   * @return true if this environment is locked. When locked, bindings cannot be added  or removed.
   */
  public final boolean isLocked() {
    return locked;
  }

  /**
   * Locking the environment prevents adding or removing variable bindings from the environment.
   * Changing the value of a variable is still possible unless the binding has been locked
   *
   * @param lockBindings true if the bindings are to be locked as well
   */
  public final void lock(boolean lockBindings) {
    this.locked = true;
    if(lockBindings) {
      lockedBindings = Sets.newHashSet(listBindings());
      if(activeBindings != null) {
        lockedBindings.addAll(activeBindings.keySet());
      }
    }
  }

  @Override
  public final int length() {
    int length = listBindings().size();
    if(activeBindings != null) {
      length += activeBindings.size();
    }
    return length;
  }

  /**
   * Returns true if the given {@code symbol} is bound to either a normal value, or
   * to an active binding in this Environment.
   *
   * <p>This call is guaranteed to be free of side-effects.</p>
   */
  public boolean exists(Symbol symbol) {
    return isBound(symbol) || isActiveBinding(symbol);
  }

  /**
   * Locking the binding prevents changing the value of the variable
   *
   * @param symbol variable symbol
   */
  public final void lockBinding(Symbol symbol) {
    if (!exists(symbol)) {
      throw new EvalException("no binding for '%s'", symbol);
    }
    if(lockedBindings == null) {
      lockedBindings = Sets.newHashSet();
    }
    lockedBindings.add(symbol);
  }

  /**
   * Unlocks the binding to allow changing the value of the given {@code symbol}.
   *
   * <p>A binding, either normal or active, <strong>must</strong> exist, or an error
   * is thrown. No error is thrown if the binding exists but is not locked.</p>
   *
   * @throws EvalException if a binding for the given {@code symbol} does not exist.
   */
  public final void unlockBinding(Symbol symbol) {

    if(!exists(symbol)) {
      throw new EvalException("no binding for '%s'", symbol);
    }

    if(lockedBindings != null) {
      lockedBindings.remove(symbol);
    }
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  public final Iterable<Environment> parents() {
    return () -> new EnvIterator(Environment.this.getParent());
  }

  /**
   * get Variable without using context (for backward compatibility)
   * Will not invoke active bindings. Please replace with getVariableUnsafe()
   *
   * @param name variable name to look up
   */
  @Deprecated
  public final SEXP getVariable(String name) {
    return getVariableUnsafe(name);
  }

  /**
   * get Variable without using context (for backward compatibility)
   * Will not invoke active bindings. Please replace with getVariableUnsafe()
   *
   * @param symbol variable name to look up
   */
  @Deprecated
  public final SEXP getVariable(Symbol symbol) {
    return getVariableUnsafe(symbol);
  }

  /**
   * Get variable value or execute function bound to a symbol. In case of normal binding return the SEXP value,
   * otherwise if its an active binding evaluate the function in provided context.
   * @param context the current evaluation context
   * @param symbol the {@code SYMSXP} that should be looked up
   * @return SEXP value of binding or result of active binding evaluation
   * @throws AssertionError if context is not provided
   */
  public final SEXP getVariable(Context context, Symbol symbol) {
    assert context != null;
    if(activeBindings != null && activeBindings.containsKey(symbol)) {
      return evaluateFunction(context, symbol);
    }
    return getBinding(symbol);
  }

  /**
   * Get variable value or execute function bound to a symbol. In case of normal binding return the SEXP value,
   * otherwise if its an active binding evaluate the function in provided context.
   * @param context the current evaluation context
   * @param symbolName the {@code SYMSXP} name that should be looked up
   * @return SEXP value of binding or result of active binding evaluation
   * @throws AssertionError if context is not provided
   */
  public final SEXP getVariable(Context context, String symbolName) {
    if(StringVector.isNA(symbolName)) {
      symbolName = "NA";
    }
    return getVariable(context, Symbol.get(symbolName));
  }

  /**
   * Get the variable assigned to a Symbol when no active bindings are present. If variable is an active binding
   * getting the variable results into a call that requires the context. Therefor, this is only used when its
   * absolutely sure that there are no active bindings present.
   *
   * @param symbol the {@code SYMSXP} that should be looked up
   * @return SEXP value
   */
  public final SEXP getVariableUnsafe(Symbol symbol) {
    return getBinding(symbol);
  }

  public final SEXP getVariableOrThrowIfActivelyBound(Symbol symbol) {
    if (isActiveBinding(symbol)) {
      throw new IllegalStateException("Encountered active binding " + symbol + " in environment " + getName());
    }
    return getBinding(symbol);
  }

  /**
   * getVariable returns the value for the provided symbol without handling active bindings.
   * Does not invoke active bindings
   *
   * @param symbolName the {@code SYMSXP} name that should be looked up
   * @return SEXP value.
   * @throws AssertionError if active bindings are present.
   */
  public SEXP getVariableUnsafe(String symbolName) {
    return getVariableUnsafe(Symbol.get(symbolName));
  }

  public SEXP getEllipsesVariable() {
    return getVariableUnsafe(Symbols.ELLIPSES);
  }

  public boolean hasVariable(Symbol symbol) {
    return isBound(symbol) || isActiveBinding(symbol);
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    unsafeSetAttributes(attributes);
    return this;
  }

  public boolean isMissingArgument(Symbol symbol) {
    return false;
  }

  private static class EnvIterator extends UnmodifiableIterator<Environment> {
    private Environment next;

    private EnvIterator(Environment next) {
      this.next = next;
    }

    @Override
    public boolean hasNext() {
      return next != EMPTY;
    }

    @Override
    public Environment next() {
      Environment toReturn = next;
      next = next.parent;
      return toReturn;
    }
  }

  @Override
  public String toString() {
    return "<environment: " + getName() + ">";
  }


  protected abstract Collection<Symbol> listBindings();

  protected abstract boolean isBound(Symbol symbol);

  protected abstract SEXP getBinding(Symbol symbol);

  protected abstract Function getFunctionBinding(Context context, Symbol symbol);

  protected abstract void removeBinding(Symbol symbol);

  protected abstract void updateBinding(Symbol symbol, SEXP value);


}
