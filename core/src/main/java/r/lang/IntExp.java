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

import cern.colt.list.IntArrayList;
import cern.colt.list.adapter.IntListAdapter;

import java.util.Iterator;

public class IntExp extends AbstractVector implements Iterable<Integer> {

  /**
   * NA_INTEGER:= INT_MIN currently
   */
  public static final int NA = Integer.MIN_VALUE;

  private IntArrayList values;

  public IntExp(int... values) {
    this.values = new IntArrayList(values);
  }

  public static SEXP parseInt(String s) {
    if (s.startsWith("0x")) {
      return new IntExp(Integer.parseInt(s.substring(2), 16));
    } else {
      return new IntExp(Integer.parseInt(s));
    }
  }

  @Override
  public int length() {
    return values.size();
  }

  @Override
  public Type getType() {
    return Type.INTSXP;
  }

  public int get(int i) {
    return values.get(i);
  }

  public void set(int i, int value) {
    values.set(i, value);
  }

  private IntExp() {
  }

  public static SEXP ofLength(int length) {
    return new IntExp(new int[length]);
  }

  @Override
  public boolean isNumeric() {
    return !inherits("factor");
  }

  @Override
  public String toString() {
    if (values.size() == 1) {
      return Integer.toString(values.get(0));
    } else {
      return values.toString();
    }
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this); 
  }

  @Override
  public Iterator<Integer> iterator() {
    return new IntListAdapter(values).iterator();
  }
}
