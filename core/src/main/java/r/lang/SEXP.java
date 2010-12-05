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

/**
 * Base class for R data types.
 */
public interface SEXP {

  public int length();

  boolean hasAttributes();

  PairList getAttributes();

  int getTypeCode();

  String getTypeName();


  /**
   * @return this expression's tag
   * @throws ClassCastException if this expression's tag is NullExp
   */
  SEXP getRawTag();

  SymbolExp getTag();

  boolean hasTag();

  void setTag(SEXP tag);

  void accept(SexpVisitor visitor);

  /**
   * Evaluates this expression in the environment rho
   *
   * @param rho the environment in which to evaluate the expression
   * @return the result
   */
  EvalResult evaluate(Environment rho);

  /**
   * Shortcut for evaluate(rho).getExpression()
   *
   * @param rho the environment in which this expression should be evaluated
   * @return
   */
  SEXP evalToExp(Environment rho);

  boolean isNumeric();


  /**
   * Coerces this {@code SEXP} to a single logical value
   * @return
   */
  Logical asLogical();

  /**
   * Coerces this {@code SEXP} to a single double value.
   */
  double asReal();

  /**
   * @return the R language class of this expression
   */
  StringVector getClassAttribute();

  boolean inherits(String sClassName);

  SEXP getNames();

  String getName(int index);

  /**
   * Searches the list of this vector's names for
   * a symbol that matches {@code name}.
   *
   *
   * @param name the name for which to search
   * @return  the index of the matching name, or -1 if
   * no match is found.
   */
  int getIndexByName(String name);

  int getIndexByName(SymbolExp name);

  SEXP getAttribute(SymbolExp name);

  SEXP setAttribute(String attributeName, SEXP value);

  SEXP setClass(StringVector classNames);

  SEXP setNames(StringVector names);

  /**
   *
   * @return an {@code Iterable} over elements within this
   * expression.
   */
  Iterable<SEXP> elements();

}
