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

import java.util.Arrays;
import java.util.Iterator;

public class StringExp extends AbstractVector implements Iterable<String> {
  String values[];

  public StringExp(String... values) {
    this.values = Arrays.copyOf(values, values.length, String[].class);
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  public Type getType() {
    return Type.STRSXP;
  }

  public String get(int i) {
    return values[i];
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<String> iterator() {
    return Iterators.forArray(values);
  }

  @Override
  public String toString() {
    if (values.length == 1) {
      return values[0];
    } else {
      return Arrays.toString(values);
    }
  }

  public static SEXP ofLength(int length) {
    return new StringExp(new String[length]);
  }
}
