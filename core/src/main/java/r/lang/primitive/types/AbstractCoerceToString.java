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

import com.google.common.collect.Iterables;
import r.lang.*;
import r.parser.ParseUtil;

import java.util.ArrayList;

public class AbstractCoerceToString extends SexpVisitor implements CoercingVisitor {

  protected final ArrayList<String> values;


  public AbstractCoerceToString() {
    this.values = new ArrayList<String>();
  }

  public AbstractCoerceToString(ArrayList<String> values) {
    this.values = values;
  }

  @Override
  public void visit(CharExp charExp) {
    values.add(charExp.getValue());
  }

  @Override
  public void visit(IntExp intExp) {
    ensureAdditionalCapacityFor(intExp);
    for(int i=0;i!=intExp.length();++i) {
      values.add(ParseUtil.toString(intExp.get(i)));
    }
  }

  @Override
  public void visit(RealExp realExp) {
    ensureAdditionalCapacityFor(realExp);
    for(int i=0;i!=realExp.length();++i) {
      values.add(ParseUtil.toString(realExp.get(i)));
    }
  }

  @Override
  public void visit(StringExp stringExp) {
    ensureAdditionalCapacityFor(stringExp);
    Iterables.addAll(values, stringExp);
  }

  @Override
  public void visit(LogicalExp logicalExp) {
    ensureAdditionalCapacityFor(logicalExp);
    for(Logical logical : logicalExp) {
      values.add(logical.toString());
    }
  }

  private void ensureAdditionalCapacityFor(SEXP exp) {
    values.ensureCapacity(values.size() + exp.length());
  }

  @Override
  public StringExp coerce() {
    return new StringExp(values);
  }
}
