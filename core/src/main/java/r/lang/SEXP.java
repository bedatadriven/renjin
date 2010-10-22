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

import r.lang.exception.LanguageException;

/**
 * 
 */
public abstract class SEXP {

  private SEXP attributes = NilExp.INSTANCE;
  private SEXP tag = NilExp.INSTANCE;
  private int named = 0;


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

  public SEXP getAttributes() {
    return attributes;
  }

  public void setAttributes(SEXP attributes) {
    this.attributes = attributes;
  }

  public abstract Type getType();

  public int getNamed() {
    return named;
  }

  public void setNamed(int named) {
    this.named = named;
  }

  public SEXP getTag() {
    return tag;
  }

  public boolean hasTag() {
    return tag != NilExp.INSTANCE;
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

  public static enum Type {

    /**
     * NULL
     */
    NILSXP(0, "NULL"),

    /**
     * Symbols
     */
    SYMSXP(1, ""),

    /**
     * Pairlists
     */
    LISTSXP(2, ""),

    /**
     * Closures
     */
    CLOSXP(3, "closure"),

    /**
     * Environments
     */
    ENVSXP(4, "environment"),

    /**
     * Promise objects
     */
    PROMSXP(5, ""),

    /**
     * Language objects
     */
    LANGSXP(6, ""),

    /**
     * Special functions
     */
    SPECIALSXP(7, ""),

    /**
     * Built in functions
     */
    BUILTINSXP(8, ""),

    /**
     * Character vectors
     */
    CHARSXP(9, ""),

    /**
     * Logical Vectors
     */
    LGLSXP(10, "logical"),

    /**
     * Integer vectors
     */
    INTSXP(13, "integer"),

    /**
     * Numeric vectors
     */
    REALSXP(14, "double"),

    /**
     * Complex vectors
     */
    CPLXSXP(15, ""),

    /**
     * VEctors of string
     */
    STRSXP(16, "character"),

    /**
     * Dot-dot-dot object
     */
    DOTSXP(17, ""),

    /**
     * make any args work
     */
    ANYSXP(18, ""),

    /**
     * list (generic vector)
     */
    VECSXP(19, ""),

    /**
     * expression vector
     */
    EXPRSXP(20, "expression"),

    /**
     * byte code
     */
    BCODESXP(21, ""),

    /**
     * EXternal pointer
     */
    EXTPTRSXP(22, ""),

    /**
     * Weak reference
     */
    WEAKREFSXP(23, ""),

    /**
     * raw vector
     */
    RAWSXP(24, "raw"),

    /**
     * S4SXP
     */
    S4SXP(25, "");


    private int code;
    private String name;

    private Type(int code, String name) {
      this.code = code;
      this.name = name;
    }

    public int getCode() {
      return code;
    }

    public String getName() {
      return name;
    }
  }

  /**
   * Evaluates this expression in the environment rho
   *
   * @param rho the environment in which to evaluate the expression
   * @return the result
   */
  public SEXP evaluate(EnvExp rho) {

    // I've ripped out all of the stack-counting stuff,
    // I'm assuming that the JVM will just throw a StackOverflowException, right...?


    /* Make sure constants in expressions are NAMED before being
  used as values.  Setting NAMED to 2 makes sure weird calls
  to assignment functions won't modify constants in
  expressions.  */
    if (getNamed() != 2) {
      setNamed(2);
    }
    return this;


// TODO: autoprinting?    R_Visible = TRUE;

  }

  public boolean isNumeric() {
    return false;
  }

  public boolean isObject() {
    return obj != 0;
  }

  public Logical asLogical() {
    return Logical.NA;
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
    throw new LanguageException(String.format("object of type '%s' is not subsettable", getType().getName()));
  }

  /**
   * Return a subset of a single element at index {@code index}.
   * @param index
   * @return
   */
  public SEXP subset(int index) {
    return subset(index, index);
  }

  public abstract void accept(SexpVisitor visitor);

}
