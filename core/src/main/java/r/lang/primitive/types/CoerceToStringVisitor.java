/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
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

package r.lang.primitive.types;

import r.lang.Logical;
import r.lang.SEXP;
import r.lang.StringExp;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class CoerceToStringVisitor extends AbstractCoerceVisitor<String> {

  private List<String> values = new ArrayList<String>();

  private static final NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance();
  private static final NumberFormat REAL_FORMAT = createRealFormat();

  private static  NumberFormat createRealFormat() {
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMinimumFractionDigits(0);
    format.setMaximumFractionDigits(15);
    return format;
  }

  public CoerceToStringVisitor(SEXP exp) {
    exp.accept(this);
  }

  @Override
  protected String coerce(String s) {
    return s;
  }

  @Override
  protected String coerce(Integer i) {
    return INTEGER_FORMAT.format(i);
  }

  @Override
  protected String coerce(Double d) {
    return REAL_FORMAT.format(d);
  }

  @Override
  protected String coerce(Logical logical) {
    return logical.toString();
  }

  @Override
  protected void collect(String value) {
    values.add(value);
  }

  @Override
  public SEXP coerce() {
    return new StringExp((String[]) values.toArray());
  }
}
