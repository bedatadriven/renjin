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

import cern.colt.list.DoubleArrayList;
import cern.colt.list.adapter.DoubleListAdapter;

import java.util.Iterator;

public final class RealExp extends AbstractVector implements Iterable<Double> {
  private DoubleArrayList values;

  public RealExp(double... values) {
    this.values = new DoubleArrayList(values);
  }


  /**
   * Returns a RealVector with a single double value parsed from the
   * supplied string.
   *
   * @param text the string representation to parse
   * @return a RealVector of length one
   */
  public static SEXP parseDouble(String text) {
    return new RealExp(Double.parseDouble(text));
  }

  @Override
  public Type getType() {
    return Type.REALSXP;
  }

  public double get(int i) {
    return values.get(i);
  }

  public void set(int i, double value) {
    values.set(i, value);
  }

  @Override
  public int length() {
    return values.size();
  }

  public static RealExp ofLength(int length) {
    return new RealExp(new double[length]);
  }

  @Override
  public boolean isNumeric() {
    return true;
  }

  @Override
  public Logical asLogical() {
    double x = values.get(0);

    if (Double.isNaN(x)) {
      return Logical.NA;
    } else if (x == 0) {
      return Logical.FALSE;
    } else {
      return Logical.TRUE;
    }
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<Double> iterator() {
    return new DoubleListAdapter(values).iterator();
  }

  @Override
  public String toString() {
    if (values.size() == 1) {
      return Double.toString(values.get(0));
    } else {
      return values.toString();
    }
  }
}
