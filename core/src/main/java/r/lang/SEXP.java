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

import r.lang.exception.EvalException;

/**
 * Base class for R data types.
 */
public abstract class SEXP {

  protected final PairList attributes;
  private SEXP tag = NullExp.INSTANCE;
  private int named = 0;

  protected SEXP(PairList attributes) {
    this.attributes = attributes;
  }

  protected SEXP() {
    this(NullExp.INSTANCE);
  }

  /**
   * So-called General Purpose bit flags
   */
  private int gp = 0;

  private int obj = 0;

  /**
   * Bit 0 is used by macros DDVAL and SET_DDVAL.
   * This indicates that a SYMSXP is one of the symbols ..n which are
   * implicitly created when ... is processed, and so indicates that
   * it may need to be looked up in a DOTSXP.
   */
  private static final int DOTDOTVALUE_MASK = 0x1;

  private static final int ACTIVE_BINDING_MASK = (1 << 15);

  private static final int BINDING_LOCK_MASK = (1 << 14);

  private static final int SPECIAL_BINDING_MASK = (ACTIVE_BINDING_MASK | BINDING_LOCK_MASK);


  public int length() {
    return 1;
  }

  public boolean hasAttributes() {
    return attributes.length() != 0;
  }

  public PairListExp getAttributes() {
    return (PairListExp)attributes;
  }

  public abstract int getTypeCode();

  public abstract String getTypeName();

  public int getNamed() {
    return named;
  }

  public void setNamed(int named) {
    this.named = named;
  }

  /**
   * @return this expression's tag
   * @throws ClassCastException if this expression's tag is NullExp
   */
  public SEXP getRawTag() {
    return tag;
  }

  public SymbolExp getTag() {
    return (SymbolExp)tag;
  }

  public boolean hasTag() {
    return tag != NullExp.INSTANCE;
  }

  public void setTag(SEXP tag) {
    this.tag = tag;
  }

  public boolean isDotDotValue() {
    return (gp & DOTDOTVALUE_MASK) != 0;
  }

  public void setDotDotValue(boolean flag) {
    if (flag) {
      gp |= DOTDOTVALUE_MASK;
    } else {
      gp &= ~DOTDOTVALUE_MASK;
    }
  }

  public boolean isActiveBinding() {
    return (gp & ACTIVE_BINDING_MASK) != 0;
  }

  public boolean isBindingLocked() {
    return (gp & BINDING_LOCK_MASK) != 0;
  }

  public void setActiveBindingBit() {
    gp |= ACTIVE_BINDING_MASK;
  }

  public void setBindingLocked(boolean locked) {
    if (locked) {
      gp |= BINDING_LOCK_MASK;
    } else {
      gp &= (~BINDING_LOCK_MASK);
    }
  }


  public abstract void accept(SexpVisitor visitor);

  /**
   * Evaluates this expression in the environment rho
   *
   * @param rho the environment in which to evaluate the expression
   * @return the result
   */
  public EvalResult evaluate(EnvExp rho) {
    return new EvalResult(this);
  }

  /**
   * Shortcut for evaluate(rho).getExpression()
   *
   * @param rho the environment in which this expression should be evaluated
   * @return
   */
  public final SEXP evalToExp(EnvExp rho) {
    return evaluate(rho).getExpression();
  }

  public boolean isNumeric() {
    return false;
  }

  public boolean isObject() {
    return obj != 0;
  }

  /**
   * Coerces this {@code SEXP} to a single logical value
   * @return
   */
  public Logical asLogical() {
    return Logical.NA;
  }

  /**
   * Coerces this {@code SEXP} to a single double value.
   */
  public double asReal() {
    return DoubleExp.NA;
  }

  public SEXP getAttribute(String name) {
    // TODO: attribs
    return null;
  }

  public boolean inherits(String sClassName) {

    // TODO: S4
//    SEXP klass;
//    int i, nclass;
//    if (isObject()) {
//      klass = getAttrib(s, R_ClassSymbol);
//      nclass = length(klass);
//      for (i = 0; i < nclass; i++) {
//        if (!strcmp(CHAR(STRING_ELT(klass, i)), name))
//          return TRUE;
//      }
//    }
    return false;
  }



  /**
   * Returns a subset from index {@code from} to index {@code to}. Indices are
   * 1-based
   *
   * @param from
   * @param to
   * @return
   */
  public SEXP subset(int from, int to) {
    throw new EvalException(String.format("object of type '%s' is not subsettable", getTypeName()));
  }

  /**
   * Return a subset of a single element at index {@code index}.
   * @param index
   * @return
   */
  public SEXP subset(int index) {
    return subset(index, index);
  }

  public final SEXP getNames() {
    return attributes.findByTag(SymbolExp.NAMES);
  }

  public String getName(int index) {
    SEXP names = attributes.findByTag(SymbolExp.NAMES);
    if(names instanceof StringExp) {
      return ((StringExp) names).get(index);
    }
    return "";
  }
}
