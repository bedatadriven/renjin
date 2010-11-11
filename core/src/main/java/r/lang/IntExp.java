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

import com.google.common.collect.UnmodifiableIterator;

import java.util.Arrays;
import java.util.Iterator;

public class IntExp extends AtomicExp implements Iterable<Integer> {

  public static final String TYPE_NAME = "integer";
  public static final int TYPE_CODE = 13;

  /**
   * NA_INTEGER:= INT_MIN currently
   */
  public static final int NA = Integer.MIN_VALUE;

  private int[] values;

  private IntExp() {
  }

  public IntExp(int... values) {
    this.values = Arrays.copyOf(values, values.length);
  }

  public static SEXP parseInt(String s) {
    if (s.startsWith("0x")) {
      return new IntExp(Integer.parseInt(s.substring(2), 16));
    } else {
      return new IntExp(Integer.parseInt(s));
    }
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

  public int get(int i) {
    return values[i];
  }

  public void set(int i, int value) {
    values[i] = value;
  }

  public static SEXP ofLength(int length) {
    return new IntExp(new int[length]);
  }

  @Override
  public boolean isNumeric() {
    return !inherits("factor");
  }

  @Override
  public double asReal() {
    if(length() == 0 || values[0] == NA) {
      return DoubleExp.NA;
    } else {
      return values[0];
    }
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new ValueIterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IntExp intExp = (IntExp) o;

    if (!Arrays.equals(values, intExp.values)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }

  @Override
  public String toString() {
    if (values.length == 1) {
      return Integer.toString(values[0]);
    } else {
      return Arrays.toString(values);
    }
  }

  public static boolean isNA(int value) {
    return value == NA;
  }

  @Override
  public Class getElementClass() {
    return Integer.TYPE;
  }

  private class ValueIterator extends UnmodifiableIterator<Integer> {
    private int i = 0;

    @Override
    public boolean hasNext() {
      return i < values.length;
    }

    @Override
    public Integer next() {
      return values[i++];
    }
  }
}
