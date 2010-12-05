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

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A vector of statements {@code LangExp}.
 *
 * <p>
 * In R one can have objects of type "expression".
 * An expression contains one or more statements (see {@link r.lang.LangExp}.
 *
 * <p>
 * A statement is a syntactically correct collection of tokens.
 * Expression objects are special language objects which contain
 * parsed but unevaluated R statements.
 *
 */
public class ExpExp extends AbstractSEXP implements RecursiveExp, Iterable<SEXP> {
  public static final String TYPE_NAME = "expression";
  public static final int TYPE_CODE = 20;

  private SEXP[] values;

  public ExpExp(SEXP[] expressions, PairList attributes) {
    super(attributes);
    this.values = Arrays.copyOf(expressions, expressions.length);
  }


  public ExpExp(Iterable<? extends SEXP> expressions, PairList attributes) {
    super(attributes);
    this.values = Iterables.toArray(expressions, SEXP.class);
  }

  public ExpExp(Iterable<? extends SEXP> expressions){
    this(expressions, NullExp.INSTANCE);
  }

  @Override
  public EvalResult evaluate(EnvExp rho) {
    EvalResult result = EvalResult.NON_PRINTING_NULL;
    for(SEXP sexp : values) {
      result = sexp.evaluate(rho);
    }
    return result;
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public int length() {
    return values.length;
  }

  public SEXP get(int index) {
    return values[index];
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (SEXP s : values) {
      sb.append("\t").append(s).append("\n");
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<SEXP> iterator() {
    return Iterators.forArray(values);
  }
}
