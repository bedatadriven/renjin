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

import java.util.Collections;

public final class Null extends AbstractSEXP implements AtomicVector, PairList {

  public static final int TYPE_CODE = 0;
  public static final String TYPE_NAME = "NULL";

  public static final Null INSTANCE = new Null();

  private Null() {

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
  public EvalResult evaluate(Environment rho) {
    return new EvalResult(this);
  }


  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public SEXP findByTag(SymbolExp symbol) {
    return this;
  }

  @Override
  public <S extends SEXP> S get(int i) {
    throw new IllegalArgumentException("List is NULL");
  }

  @Override
  public Iterable<SEXP> values() {
    return Collections.emptySet();
  }

  @Override
  public SEXP getElementAsSEXP(int index) {
    throw new ArrayIndexOutOfBoundsException(index);
  }

  @Override
  public Vector.Builder newBuilder(int initialSize) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isWiderThan(Object vector) {
    return false;
  }

  @Override
  public Vector.Builder newCopyBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterable<Node> nodes() {
    return Collections.emptySet();
  }

  @Override
  public boolean isElementNA(int index) {
    throw new ArrayIndexOutOfBoundsException(index);
  }

}
