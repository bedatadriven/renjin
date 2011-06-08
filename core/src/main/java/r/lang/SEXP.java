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
 * Base interface for all R data types.
 */
public interface SEXP {

  /**
   * Returns the number of elements in this expression. All R objects are considered
   * to have length,
   *
   * @return the number of elements in this expression.
   */
  public int length();

  boolean hasAttributes();

  PairList getAttributes();

  String getTypeName();

  void accept(SexpVisitor visitor);

  /**
   * Evaluates this expression in the environment rho
   *
   *
   * @param context
   * @param rho the environment in which to evaluate the expression
   * @return the result
   */
  EvalResult evaluate(Context context, Environment rho);

  /**
   * Shortcut for evaluate(rho).getExpression()
   *
   *
   * @param context
   * @param rho the environment in which this expression should be evaluated
   * @return
   */
  SEXP evalToExp(Context context, Environment rho);

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

  /**
   *
   * @return the {@link StringVector} containing the element's names, or {@code NULL} if
   * this expression has no explicit {@code names} attribute.
   */
  AtomicVector getNames();

  /**
   * @param index zero-based index
   * @return the name of the element at index {@code index}, or the empty string
   * if the element has no name
   */
  String getName(int index);

  /**
   * Searches the list of this expression's {@link Attributes#NAMES} attribute for the
   * provided {@code name}.
   *
   * @param name the name for which to search
   * @return  the index of the matching name, or -1 if
   * no match is found.
   */
  int getIndexByName(String name);

  /**
   * Returns the value of the attribute named {@code name} for this
   * SEXP. Attributes are like metadata and can be associated with
   * most types of {@code SEXP}s.
   *
   * @param name the name of the attribute
   * @return  the value of the attribute, or {@code Null.INSTANCE} if the
   * attribute is not set.
   */
  SEXP getAttribute(Symbol name);

  /**
   * Copies this {@code SEXP}, sets the value of the attribute indicated
   * by {@code name}, and returns the copied object.
   *
   * @param attributeName
   * @param value  the new value, or {@code Null.INSTANCE} if the attribute should be removed (if present)
   * @return a copy of this {@code SEXP}
   */
  SEXP setAttribute(String attributeName, SEXP value);

  /**
   * Replaces all of this {@code SEXP}'s attributes with the attributes
   * specified by {@code attributes} {@code ListVector}.
   *
   * @param attributes a list of attributes. All elements must be named.
   * @return a copy of this {@code SEXP} with the new attributes
   */
  SEXP setAttributes(ListVector attributes);


  /**
   *
   * @return an {@code Iterable} over elements within this
   * expression.
   */
  Iterable<SEXP> elements();

  /**
   * @param index zero-based index of the element
   * @return the element at {@code index} as a {@link r.lang.SEXP},
   * wrapping the element if necessary in a new {@link r.lang.AtomicVector} if necessary
   */
  <S extends SEXP> S getElementAsSEXP(int index);
}
