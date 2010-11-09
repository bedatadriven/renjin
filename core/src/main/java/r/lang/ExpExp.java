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

import java.util.ArrayList;
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
public class ExpExp extends SEXP implements RecursiveExp, Iterable<SEXP> {
  public static final String TYPE_NAME = "expression";
  public static final int TYPE_CODE = 20;

  private ArrayList<SEXP> list;

  public ExpExp() {
    list = new ArrayList<SEXP>();
  }

  public ExpExp(Iterable<? extends SEXP> expressions) {
    list = new ArrayList<SEXP>();
    for (SEXP sexp : expressions) {
      list.add(sexp);
    }
  }

  @Override
  public EvalResult evaluate(EnvExp rho) {
    EvalResult result = EvalResult.NON_PRINTING_NULL;
    for(SEXP sexp : list) {
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
    return list.size();
  }

  public void add(SEXP result) {
    list.add(result);
  }

  public SEXP get(int index) {
    return list.get(index);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (SEXP s : list) {
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
    return list.iterator();
  }
}
