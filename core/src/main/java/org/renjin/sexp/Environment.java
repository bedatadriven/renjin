/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package org.renjin.sexp;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import org.renjin.base.BaseFrame;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;

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
  private Environment baseEnvironment;
  protected Frame frame;

  private boolean locked;
  private Set<Symbol> lockedBindings;

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
  public static Environment createGlobalEnvironment() {
    Environment global = new Environment();
    global.name = GLOBAL_ENVIRONMENT_NAME;
    global.baseEnvironment = createBaseEnvironment();
    global.parent = global.baseEnvironment;
    global.frame = new HashFrame();

    return global;
  }

  private static Environment createBaseEnvironment() {
    Environment base = new Environment();
    base.name = "base";
    base.baseEnvironment = base;
    base.parent = EMPTY;
    base.frame = new BaseFrame();
    return base;
  }

  public static Environment createChildEnvironment(Environment parent) {
    return createChildEnvironment(parent, new HashFrame());
  }

  public static Environment createNamespaceEnvironment(Environment parent, String namespaceName) {
    Environment ns = createChildEnvironment(parent);
    ns.name = "namespace:" + namespaceName;
    return ns;
  }
  
  public static Environment createNamedEnvironment(Environment parent, String name) {
    Environment ns = createChildEnvironment(parent);
    ns.name = name;
    return ns;
  }
  
  public static Environment createBaseNamespaceEnvironment(Environment globalEnv) {
    Environment ns = createChildEnvironment(globalEnv, globalEnv.baseEnvironment.getFrame());
    ns.name = "namespace:base";
    return ns;
  }

  public static Environment createChildEnvironment(Environment parent, Frame frame) {
    Environment child = new Environment();
    child.baseEnvironment = parent.baseEnvironment;
    child.parent = parent;
    child.frame = frame;
    return child;
  }
  
  public void setVariables(PairList pairList) {
    for(PairList.Node node : pairList.nodes()) {
      if(!node.hasTag()) {
        throw new IllegalArgumentException("All elements of pairList must be tagged");
      }
      setVariable(node.getTag(), node.getValue());
    }
  }
  

  public void remove(Symbol symbol) {
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

  public Environment getParent() {
    return parent;
  }

  public void setParent(Environment parent) {
    this.parent = parent;
    modCount ++;
  }

  public Environment getBaseEnvironment() {
    return baseEnvironment;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  public Collection<Symbol> getSymbolNames() {
    List<Symbol> ordered = new ArrayList<Symbol>(frame.getSymbols());
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

  public boolean bindingIsLocked(Symbol symbol) {
    return lockedBindings != null && lockedBindings.contains(symbol);
  }
  
  public void setVariable(String name, SEXP value) {
    setVariable(Symbol.get(name), value);
  }

  public void setVariable(Symbol symbol, SEXP value) {
    if(bindingIsLocked(symbol)) {
      throw new EvalException("cannot change value of locked binding for '%s'", symbol.getPrintName());
    } else if(locked && frame.getVariable(symbol) != Symbol.UNBOUND_VALUE) {
      throw new EvalException("cannot add bindings to a locked environment");
    }
    frame.setVariable(symbol, value);
    modCount++;
  }

  /**
   * Searches the environment for a value that matches the given predicate.
   *
   *
   *
   * @param context
   * @param symbol The symbol for which to search
   * @param predicate a predicate that tests possible return values
   * @param inherits if {@code true}, enclosing frames are searched
   * @return the bound value or {@code Symbol.UNBOUND_VALUE} if not found
   */
  public SEXP findVariable(Context context, Symbol symbol, Predicate<SEXP> predicate, boolean inherits) {
    SEXP value = frame.getVariable(symbol);
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
   * Recursively searches this environment and its parent for the symbol {@code symbol}
   * 
   * @param symbol the symbol for which to search
   * @return the bound value, or {@code Symbol.UNBOUND_VALUE} if not found
   */
  public SEXP findVariable(Symbol symbol) {
    if(symbol.isVarArgReference()) {
      return findVarArg(symbol.getVarArgReferenceIndex());
    }
    SEXP value = frame.getVariable(symbol);
    if(value != Symbol.UNBOUND_VALUE) {
      return value;
    }
    return parent.findVariable(symbol);
  }

  private SEXP findVarArg(int varArgReferenceIndex) {
    SEXP ellipses = findVariable(Symbols.ELLIPSES);
    if(ellipses == Symbol.UNBOUND_VALUE) {
      throw new EvalException("..%d used in an incorrect context, no ... to look in", varArgReferenceIndex);
    }
    PairList varArgs = (PairList) ellipses;
    if(varArgs.length() < varArgReferenceIndex) {
      throw new EvalException("The ... list does not contain %d items", varArgReferenceIndex);
    }
    return varArgs.getElementAsSEXP(varArgReferenceIndex - 1);
  }

  public SEXP findVariableOrThrow(String name) {
    SEXP value = findVariable(Symbol.get(name));
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("object '" + name + "' not found");
    }
    return value;
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
    }
  }

  public void lockBinding(Symbol symbol) {
    if(frame.getVariable(symbol) == Symbol.UNBOUND_VALUE) {
      throw new EvalException("no binding for '%s'", symbol);
    }
    if(lockedBindings == null) {
      lockedBindings = Sets.newHashSet();
    }
    lockedBindings.add(symbol);
  }

  public void unlockBinding(Symbol symbol) {
    if(frame.getVariable(symbol) == Symbol.UNBOUND_VALUE) {
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

  public SEXP getVariable(Symbol symbol) {
    return frame.getVariable(symbol);
  }

  public SEXP getVariable(String symbolName) {
    return getVariable(Symbol.get(symbolName));
  }

  /**
   * Finds a variable by prefix, for example, "x" will
   * match "xx" and "i" will match imaginary.
   * @param prefix
   * @return the first matching binding, or NULL if there
   * are no matching bindings
   */
  public SEXP getVariableByPrefix(String prefix) {
    SEXP value = null;
    if(frame.getSymbols().contains(Symbol.get(prefix))) {
      return frame.getVariable(Symbol.get(prefix));
    }
    for(Symbol name : frame.getSymbols()) {
      if(name.getPrintName().startsWith(prefix)) {
        return frame.getVariable(name);
      }
    }
    return Null.INSTANCE;
  }


  public boolean hasVariable(Symbol symbol) {
    return frame.getVariable(symbol) != Symbol.UNBOUND_VALUE;
  }
  

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    this.attributes = attributes;
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
  	Environment newEnv = Environment.createChildEnvironment(parent, frame);
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
    public SEXP findVariable(Symbol symbol) {
      return Symbol.UNBOUND_VALUE;
    }

    @Override
    public SEXP getVariable(Symbol symbol) {
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
      boundValue.value = getVariable(name);
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

}
