/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

import org.renjin.base.BaseFrame;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.primitives.Contexts;
import org.renjin.primitives.Evaluation;
import org.renjin.primitives.Native;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.repackaged.guava.collect.UnmodifiableIterator;

import java.util.*;

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
public class Environment extends AbstractSEXP implements Recursive, HasNamedValues {


  public static final String TYPE_NAME = "environment";
  private static final String GLOBAL_ENVIRONMENT_NAME = "R_GlobalEnv";

  private String name = null;
  private Environment parent;
  protected Frame frame;

  private boolean locked;
  private Set<Symbol> lockedBindings;
  private Map<Symbol, Closure> activeBindings = null;

  /**
   * Keeps track of the number of times setVariable() has been called on this 
   * environment.
   */
  private transient int modCount = 0;
  
  /**
   * The root of the environment hierarchy.
   */
  public static final EmptyEnv EMPTY = new EmptyEnv();


  /**
   * Creates a new tree of environments, initialized with
   * the empty, base, and global environments:
   *
   * <pre>
   * &lt;EmptyEnvironment&gt;
   *        |
   *  &lt;package:base&gt;
   *        |
   *   &lt;GlobalEnv&gt;
   * </pre>
   *
   * @return the Global environment
   */
  public static Environment createGlobalEnvironment(Environment baseEnvironment, Frame frame) {
    Environment global = new Environment(frame);
    global.name = GLOBAL_ENVIRONMENT_NAME;
    global.parent = baseEnvironment;

    return global;
  }

  public static Environment createGlobalEnvironment(Environment baseEnvironment) {
    return createGlobalEnvironment(baseEnvironment, new HashFrame());
  }

  public static Environment createBaseEnvironment() {
    Environment base = new Environment(new BaseFrame());
    base.name = "base";
    base.parent = EMPTY;
    return base;
  }

  public static Builder createChildEnvironment(Environment parent) {
    return createChildEnvironment(parent, new HashFrame());
  }

  public static Builder createNamespaceEnvironment(Environment parent, String namespaceName) {
    Builder ns = createChildEnvironment(parent);
    ns.name = "namespace:" + namespaceName;
    return ns;
  }
  
  public static Builder createNamedEnvironment(Environment parent, String name) {
    Builder ns = createChildEnvironment(parent);
    ns.name = name;
    return ns;
  }
  
  public static Builder createBaseNamespaceEnvironment(Environment globalEnv, Environment baseEnvironment) {
    Builder ns = createChildEnvironment(globalEnv, baseEnvironment.getFrame());
    ns.name = "namespace:base";
    return ns;
  }

  public static Builder createChildEnvironment(Environment parent, Frame frame) {
    return new Builder(parent, frame);
  }

  private Environment(Frame frame) {
    this.frame = frame;
  }

  private Environment() {
    this.frame = new HashFrame();
  }

  public Environment(AttributeMap attributes) {
    super(attributes);
    this.frame = new HashFrame();
  }

  /**
   * set multiple variables at the same time.
   * if variable is an active binding it will be invoked
   * @param context
   * @param pairList
   */
  public void setVariables(Context context, PairList pairList) {
    for(PairList.Node node : pairList.nodes()) {
      if(!node.hasTag()) {
        throw new IllegalArgumentException("All elements of pairList must be tagged");
      }
      setVariable(context, node.getTag(), node.getValue());
    }
  }

  public void remove(Symbol symbol) {
    if(locked) {
      throw new EvalException("cannot remove bindings from a locked environment");
    }
    if(isActiveBinding(symbol)) {
      activeBindings.remove(symbol);
    }
    frame.remove(symbol);
  }

  public void clear() {
    frame.clear();
  }

  public String getName() {
    SEXP nameAttribute = this.attributes.get(Symbols.NAME);
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
  public Environment getParent() {
    return parent;
  }

  /**
   * set parent environment to provided environment
   *
   * @param parent environment to be set as parent environment
   */
  public void setParent(Environment parent) {
    this.parent = parent;
    modCount ++;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  public Collection<Symbol> getSymbolNames() {
    List<Symbol> ordered = new ArrayList<Symbol>(frame.getSymbols());
    if(activeBindings != null) {
      List<Symbol> ordered2 = new ArrayList<Symbol>(activeBindings.keySet());
      ordered.addAll(ordered2);
    }

    Collections.sort(ordered,new Comparator<Symbol>(){
      @Override
      public int compare(Symbol o1, Symbol o2) {
        if(o1.getPrintName().startsWith(".") && !o2.getPrintName().startsWith(".")){
          return 1;
        }else if(!o1.getPrintName().startsWith(".") && o2.getPrintName().startsWith(".")){
          return -1;
        }else{
          return o1.getPrintName().compareTo(o2.getPrintName());
        }
      }
      
    });
    return ordered;
  }
  
  @Override
  public StringVector getNames() {
    StringVector.Builder names = new StringVector.Builder();
    for (Symbol name : getSymbolNames()) {
      names.add(name.getPrintName());
    }
    return names.build();
  }

  public boolean bindingIsLocked(Symbol symbol) {
    return lockedBindings != null && lockedBindings.contains(symbol);
  }

  /**
   * set Variable without checking if variable is an 'active' binding.
   * Only be used when it is absolutely certain that the Symbol is not used in any active binding.
   *
   * @param symbol  the {@code SYMSXP} that should be looked up
   * @param value value to be assigned.
   * @throws AssertionError when active bindings are present.
   */
  public void setVariableUnsafe(Symbol symbol, SEXP value) {
    assert ( !isActiveBinding(symbol) );

    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("Unbound: " + symbol);
    }

    if(bindingIsLocked(symbol)) {
      throw new EvalException("cannot change value of locked binding for '%s'", symbol.getPrintName());
    } else if(locked && frame.getVariable(symbol) == Symbol.UNBOUND_VALUE) {
      throw new EvalException("cannot add bindings to a locked environment");
    }
    frame.setVariable(symbol, value);
    modCount++;
  }

  /**
   * set Variable without checking if variable is an 'active' binding.
   * Only be used when it is absolutely certain that the Symbol is not used in any active binding.
   *
   * @param name variable name.
   * @param value value to be assigned.
   * @throws AssertionError when active bindings are present.
   */
  public void setVariableUnsafe(String name, SEXP value) {
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
  public void setVariable(String name, SEXP value) {
    setVariableUnsafe(name, value);
  }

  @Deprecated
  public void setVariable(Symbol symbol, SEXP value) {
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
  public SEXP setVariable(Context context, String name, SEXP value) {
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
  public SEXP setVariable(Context context, Symbol symbol, SEXP value) {
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

    if(locked && frame.getVariable(symbol) == Symbol.UNBOUND_VALUE) {
      throw new EvalException("cannot add bindings to a locked environment");
    }

    frame.setVariable(symbol, value);
    modCount++;
    return Null.INSTANCE;
  }

  /**
   * Creates an active binding to given variable name
   *
   * @param symbol variable name
   * @param closure function closure to bind to variable
   */
  public void makeActiveBinding(Symbol symbol, Closure closure) {
    if(bindingIsLocked(symbol)) {
      throw new EvalException("cannot change value of locked binding for '%s'", symbol.getPrintName());
    } else if(locked && frame.getVariable(symbol) == Symbol.UNBOUND_VALUE) {
      throw new EvalException("cannot add bindings to a locked environment");
    }
    if(frame.getSymbols().contains(symbol)) {
      throw new EvalException("Error in makeActiveBinding(%s, %s, %s) :\n   symbol already has a regular binding",
        symbol.getPrintName(), closure.getTypeName(), closure.getEnclosingEnvironment().getTypeName());
    }
    if(activeBindings == null) {
      activeBindings = new HashMap<Symbol, Closure>();
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
      if(predicate.apply(value)) {
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
    SEXP value = frame.getVariable(symbol);
    if(value != Symbol.UNBOUND_VALUE) {
      return value;
    }
    return parent.findVariable(context, symbol);
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
  public SEXP findVariable(Symbol symbol) {
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
  public SEXP findVariableUnsafe(Symbol symbol) {
    if(symbol.isVarArgReference()) {
      return findVarArg(symbol.getVarArgReferenceIndex());
    }
    assert ( !isActiveBinding(symbol) );
    SEXP value = frame.getVariable(symbol);
    if(value != Symbol.UNBOUND_VALUE) {
      return value;
    }
    return parent.findVariableUnsafe(symbol);
  }


  /**
   * returns varArg value at provided index
   *
   * @param varArgReferenceIndex index of varArg to return
   * @return
   */
  private SEXP findVarArg(int varArgReferenceIndex) {
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
  public SEXP findVariableOrThrow(Context context, Symbol symbol) {
    SEXP value = findVariable(context, symbol);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("object '" + symbol.getPrintName() + "' not found");
    }
    return value;
  }


  /**
   * return variable value or invoke associated active binding and otherwise throw error
   *
   * @param context
   * @param name the {@code SYMSXP} name that should be looked up
   * @return
   * @throws EvalException if variable is not found
   */
  public SEXP findVariableOrThrow(Context context, String name) {
    return findVariableOrThrow(context, Symbol.get(name));
  }

  /**
   * return variable value or otherwise throw error (for backward compatibility)
   * does not look for active bindings
   *
   * @param symbol the {@code SYMSXP} that should be looked up
   * @return
   * @throws EvalException if variable is not found
   */
  public SEXP findVariableOrThrowUnsafe(Symbol symbol) {
    SEXP value = findVariableUnsafe(symbol);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("object '" + symbol.getPrintName() + "' not found");
    }
    return value;
  }


  /**
   * return variable value or otherwise throw error (for backward compatibility)
   * does not look for active bindings
   *
   * @param name the {@code SYMSXP} name that should be looked up
   * @return
   * @throws EvalException if variable is not found
   */
  public SEXP findVariableOrThrowUnsafe(String name) {
    return findVariableOrThrowUnsafe(Symbol.get(name));
  }

  public Function findFunction(Context context, Symbol symbol) {
    if(frame.isMissingArgument(symbol)) {
      throw new EvalException("argument '%s' is missing, with no default", symbol.toString());
    }
    Function value = frame.getFunction(context, symbol);
    if(value != null) {
      return value;
    }
    return parent.findFunction(context, symbol);
  }
  
  public Function findFunctionOrThrow(Context context, Symbol symbol) {
    Function function = findFunction(context, symbol);
    if(function == null) {
      throw new EvalException("could not find function \"" + symbol + "\"");
    }
    return function;
  }

  /**
   *
   * @return true if this environment is locked. When locked, bindings cannot be added  or removed.
   */
  public boolean isLocked() {
    return locked;
  }

  /**
   * 
   * @return the number of modifications to this environment
   * and all of its parent environments
   */
  public int getCumulativeModCount() {
    return modCount + parent.getCumulativeModCount();
  }
  
  public Frame getFrame() {
    return frame;
  }
  
  /**
   * Locking the environment prevents adding or removing variable bindings from the environment.
   * Changing the value of a variable is still possible unless the binding has been locked
   *
   * @param lockBindings true if the bindings are to be locked as well
   */
  public void lock(boolean lockBindings) {
    this.locked = true;
    if(lockBindings) {
      lockedBindings = Sets.newHashSet(frame.getSymbols());
      if(activeBindings != null) {
        lockedBindings.addAll(activeBindings.keySet());
      }
    }
  }

  /**
   * Returns true if the given {@code symbol} is bound to either a normal value, or
   * to an active binding in this Environment.
   */
  public boolean exists(Symbol symbol) {
    return frame.getVariable(symbol) != Symbol.UNBOUND_VALUE ||
        isActiveBinding(symbol);
  }

  /**
   * Locking the binding prevents changing the value of the variable
   *
   * @param symbol variable symbol
   */
  public void lockBinding(Symbol symbol) {
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
  public void unlockBinding(Symbol symbol) {

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

  public Iterable<Environment> parents() {
    return new Iterable<Environment>() {
      @Override
      public Iterator<Environment> iterator() {
        return new EnvIterator(Environment.this.getParent());
      }
    };
  }

  /**
   * get Variable without using context (for backward compatibility)
   * Will not invoke active bindings. Please replace with getVariableUnsafe()
   *
   * @param name variable name to look up
   */
  @Deprecated
  public SEXP getVariable(String name) {
    return getVariableUnsafe(name);
  }

  /**
   * get Variable without using context (for backward compatibility)
   * Will not invoke active bindings. Please replace with getVariableUnsafe()
   *
   * @param symbol variable name to look up
   */
  @Deprecated
  public SEXP getVariable(Symbol symbol) {
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
  public SEXP getVariable(Context context, Symbol symbol) {
    assert ( context != null );
    if(activeBindings != null && activeBindings.containsKey(symbol)) {
      return evaluateFunction(context, symbol);
    }
    return frame.getVariable(symbol);
  }


  /**
   * Get variable value or execute function bound to a symbol. In case of normal binding return the SEXP value,
   * otherwise if its an active binding evaluate the function in provided context.
   * @param context the current evaluation context
   * @param symbolName the {@code SYMSXP} name that should be looked up
   * @return SEXP value of binding or result of active binding evaluation
   * @throws AssertionError if context is not provided
   */
  public SEXP getVariable(Context context, String symbolName) {
    return getVariable(context, Symbol.get(symbolName));
  }

  /**
   * Get the variable assigned to a Symbol when no active bindings are present. If variable is an active binding
   * getting the variable results into a call that requires the context. Therefor, this is only used when its
   * absolutely sure that there are no active bindings present.
   *
   * @param symbol the {@code SYMSXP} that should be looked up
   * @return SEXP value
   * @throws AssertionError if symbol is active binding.
   */
  public SEXP getVariableUnsafe(Symbol symbol) {
    assert ( !isActiveBinding(symbol) );
    return frame.getVariable(symbol);
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
    return frame.getVariable(symbol) != Symbol.UNBOUND_VALUE;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    unsafeSetAttributes(attributes);
    return this;
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

  public Environment insertAbove(Frame frame) {
    Environment newEnv = Environment.createChildEnvironment(parent, frame).build();
    setParent(newEnv);
    return newEnv;
  }

  private static class EmptyEnv extends Environment {

    private EmptyEnv() {
    }

    @Override
    public Collection<Symbol> getSymbolNames() {
      return Collections.emptySet();
    }

    @Override
    public SEXP findVariable(Context context, Symbol symbol, Predicate<SEXP> predicate, boolean inherits) {
      return Symbol.UNBOUND_VALUE;
    }

    @Override
    public SEXP findVariable(Context context, Symbol symbol) {
      return Symbol.UNBOUND_VALUE;
    }

    @Override
    public SEXP findVariableUnsafe(Symbol symbol) {
      return Symbol.UNBOUND_VALUE;
    }

    @Override
    public SEXP getVariable(Context context, Symbol symbol) {
      return Symbol.UNBOUND_VALUE;
    }

    @Override
    public int getCumulativeModCount() {
      return 0;
    }

    @Override
    public Function findFunction(Context context, Symbol symbol) {
      return null;
    }

    @Override
    public Environment getParent() {
      throw new UnsupportedOperationException("The empty environment does not have a parent.");
    }

    @Override
    public void setParent(Environment parent) {
      throw new UnsupportedOperationException("The empty environment does not have a parent.");
    }
  }

  @Override
  public Iterable<NamedValue> namedValues() {
    return new NamedValues();
  }
  
  private class NamedValues implements Iterable<NamedValue> {

    @Override
    public Iterator<NamedValue> iterator() {
      return new NamedValueIterator();
    }
    
  }

  private class NamedValueIterator extends UnmodifiableIterator<NamedValue> {

    private Iterator<Symbol> names;
    
    private NamedValueIterator() {
      this.names = getSymbolNames().iterator();
    }
    
    @Override
    public boolean hasNext() {
      return names.hasNext();
    }

    @Override
    public NamedValue next() {
      BoundValue boundValue = new BoundValue();
      Symbol name = names.next();
      boundValue.name = name;
      boundValue.value = getVariableUnsafe(name);
      return boundValue;
    }
    
  }
  
  private static class BoundValue implements NamedValue {

    private Symbol name;
    private SEXP value;
    
    @Override
    public boolean hasName() {
      return true;
    }

    @Override
    public String getName() {
      return name.getPrintName();
    }

    @Override
    public SEXP getValue() {
      return value;
    } 
    
  }

  /**
   *
   */
  public static class Builder {


    private final Environment parent;
    private final Frame frame;
    public String name;

    public Builder(Environment parent, Frame frame) {

      this.parent = parent;
      this.frame = frame;
    }

    public Builder setVariable(Symbol symbol, SEXP value) {
      frame.setVariable(symbol, value);
      return this;
    }

    public Environment build() {
      Environment child = new Environment(frame);
      child.parent = parent;
      return child;
    }
  }

  private SEXP evaluateFunction(Context context, Symbol symbol) {
    Closure fun = activeBindings.get(symbol);
    PairList.Builder args = new PairList.Builder();
    return context.evaluate(new FunctionCall(fun, args.build()));
  }

}
