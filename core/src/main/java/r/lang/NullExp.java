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

import com.google.common.collect.Iterators;

import java.util.Collections;
import java.util.Iterator;

public final class NullExp extends SEXP implements PairList, AtomicExp {

  public static final int TYPE_CODE = 0;
  public static final String TYPE_NAME = "NULL";

  public static final NullExp INSTANCE = new NullExp();

  private NullExp() {

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
    return 0;
  }

  @Override
  public String toString() {
    return "NULL";
  }

  @Override
  public EvalResult evaluate(EnvExp rho) {
    return new EvalResult(this);
  }

  @Override
  public SEXP getAttribute(String name) {
    return NullExp.INSTANCE;
  }

  @Override
  public SEXP subset(int from, int to) {
    return this;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public <S extends SEXP> S get(int i) {
    throw new IllegalArgumentException("List is NULL");
  }

  @Override
  public Iterable<PairListExp> listNodes() {
    return Collections.emptySet();
  }

  @Override
  public SEXP getFirst() {
    throw new IllegalArgumentException("List is NULL");
  }

  @Override
  public SEXP getSecond() {
    throw new IllegalArgumentException("List is NULL");
  }

  @Override
  public SEXP getThird() {
    throw new IllegalArgumentException("List is NULL");
  }

  @Override
  public Iterator<SEXP> iterator() {
    return Iterators.emptyIterator();
  }
}
