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

package r.lang;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import r.lang.primitive.BaseFrame;

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
public class EnvExp extends SEXP implements RecursiveExp {

  public static final int TYPE_CODE = 4;
  public static final String TYPE_NAME = "environment";

  private String name;
  private EnvExp parent;
  private EnvExp globalEnvironment;
  private EnvExp baseEnvironment;

  private List<SEXP> onExit = Lists.newArrayList();


  /**
   * The root of the environment hierarchy.
   */
  public static final EmptyEnv EMPTY = new EmptyEnv();

  public static EnvExp createGlobalEnvironment() {
    EnvExp global = new EnvExp();
    global.name = "R_GlobalEnv";
    global.baseEnvironment = createBaseEnvironment(global);
    global.globalEnvironment = global;
    global.parent = global.baseEnvironment;
    global.frame = new HashFrame();

    return global;
  }

  private static EnvExp createBaseEnvironment(EnvExp global) {
    EnvExp base = new EnvExp();
    base.name = "base";
    base.baseEnvironment = base;
    base.globalEnvironment = global;
    base.parent = EMPTY;
    base.frame = BaseFrame.INSTANCE;
    return base;
  }

  public static EnvExp createChildEnvironment(EnvExp parent) {
    EnvExp child = new EnvExp();
    child.name = Integer.toString(child.hashCode());
    child.baseEnvironment = parent.baseEnvironment;
    child.globalEnvironment = parent.globalEnvironment;
    child.parent = parent;
    child.frame = new HashFrame();
    return child;
  }

  public void setVariables(PairList pairList) {
    for(PairListExp node : pairList.listNodes()) {
      if(!node.hasTag()) {
        throw new IllegalArgumentException("All elements of pairList must be tagged");
      }
      setVariable(node.getTag(), node.getValue());
    }
  }



  public interface Frame {
    Set<SymbolExp> getSymbols();
    SEXP getVariable(SymbolExp name);
    SEXP getInternal(SymbolExp name);
    void setVariable(SymbolExp name, SEXP value);
  }

  public static class HashFrame implements Frame{
    private HashMap<SymbolExp, SEXP> values = new HashMap<SymbolExp, SEXP>();

    @Override
    public Set<SymbolExp> getSymbols() {
      return values.keySet();
    }

    @Override
    public SEXP getVariable(SymbolExp name) {
      SEXP value = values.get(name);
      return value == null ? SymbolExp.UNBOUND_VALUE : value;
    }

    @Override
    public SEXP getInternal(SymbolExp name) {
      return SymbolExp.UNBOUND_VALUE;
    }

    @Override
    public void setVariable(SymbolExp name, SEXP value) {
      values.put(name, value);
    }
  }

  protected Frame frame;

  public String getName() {
    return name;
  }

  public EnvExp getParent() {
    return parent;
  }

  public void setParent(EnvExp parent) {
    this.parent = parent;
  }

  public EnvExp getGlobalEnvironment() {
    return globalEnvironment;
  }

  public EnvExp getBaseEnvironment() {
    return baseEnvironment;
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  public Collection<SymbolExp> getSymbolNames() {
    return frame.getSymbols();
  }

  public void setVariable(SymbolExp symbol, SEXP value) {
    frame.setVariable(symbol, value);
  }

  /**
   * Searches the environment for a value that matches the given predicate.
   *
   * @param symbol The symbol for which to search
   * @param predicate a predicate that tests possible return values
   * @param inherits if {@code true}, enclosing frames are searched
   * @return
   */
  public SEXP findVariable(SymbolExp symbol, Predicate<SEXP> predicate, boolean inherits) {
    SEXP value = frame.getVariable(symbol);
    if(value != SymbolExp.UNBOUND_VALUE && predicate.apply(value)) {
      return value;
    }
    return parent.findVariable(symbol, predicate, inherits);
  }

  public final SEXP findVariable(SymbolExp symbol) {
    return findVariable(symbol, Predicates.<SEXP>alwaysTrue(), true);
  }

  public SEXP findInternal(SymbolExp symbol) {
    SEXP value = frame.getInternal(symbol);
    if(value != SymbolExp.UNBOUND_VALUE) {
      return value;
    }
    return parent.findInternal(symbol);
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  public Iterable<EnvExp> selfAndParents() {
    return new Iterable<EnvExp>() {
      @Override
      public Iterator<EnvExp> iterator() {
        return new EnvIterator(EnvExp.this);
      }
    };
  }

  public SEXP getVariable(SymbolExp symbol) {
    return frame.getVariable(symbol);
  }

  public boolean hasVariable(SymbolExp symbol) {
    return frame.getVariable(symbol) != SymbolExp.UNBOUND_VALUE;
  }
  
  private static class EnvIterator extends UnmodifiableIterator<EnvExp> {
    private EnvExp next;

    private EnvIterator(EnvExp next) {
      this.next = next;
    }

    @Override
    public boolean hasNext() {
      return next != EMPTY;
    }

    @Override
    public EnvExp next() {
      EnvExp toReturn = next;
      next = next.parent;
      return toReturn;
    }
  }

  public void setOnExit(SEXP exp) {
    onExit = Lists.newArrayList(exp);
  }

  public void addOnExit(SEXP exp) {
    onExit.add(exp);
  }

  public void exit() {
    for(SEXP exp : onExit) {
      exp.evaluate(this);
    }
  }


  private static class EmptyEnv extends EnvExp {

    private EmptyEnv() {
    }

    @Override
    public SEXP findVariable(SymbolExp symbol, Predicate<SEXP> predicate, boolean inherits) {
      return SymbolExp.UNBOUND_VALUE;
    }

    @Override
    public SEXP getVariable(SymbolExp symbol) {
      return SymbolExp.UNBOUND_VALUE;
    }

    @Override
    public SEXP findInternal(SymbolExp symbol) {
      return SymbolExp.UNBOUND_VALUE;
    }

    @Override
    public EnvExp getParent() {
      throw new UnsupportedOperationException("The empty environment does not have a parent.");
    }

    @Override
    public void setParent(EnvExp parent) {
      throw new UnsupportedOperationException("The empty environment does not have a parent.");
    }
  }
}