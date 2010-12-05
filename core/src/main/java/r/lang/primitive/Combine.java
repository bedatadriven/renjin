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

package r.lang.primitive;

import r.lang.*;
import r.lang.primitive.annotations.ArgumentList;
import r.lang.primitive.binding.AtomicAccessor;
import r.lang.primitive.binding.AtomicAccessors;
import r.lang.primitive.binding.AtomicBuilder;
import r.lang.primitive.binding.AtomicBuilders;

import java.util.ArrayList;
import java.util.List;

import static r.lang.primitive.binding.AtomicExps.elementClassOf;

public class Combine {


  public static SEXP combine(@ArgumentList PairList argList) {

    if(argList.length() == 0) {
      return NullExp.INSTANCE;
    }

    Inspector inspector = new Inspector((PairListExp) argList);

    Class<? extends SEXP> lowestCommonType = inspector.getLowestCommonType();
    if(AtomicVector.class.isAssignableFrom(lowestCommonType)) {
      if(inspector.getTotalLength() == 0) {
        return NullExp.INSTANCE;
      } else {
        return combineToAtomic(inspector);
      }
    } else {
      return combineToList(inspector);
    }
  }

  private static SEXP combineToAtomic(Inspector inspector) {
    AtomicBuilder builder = AtomicBuilders.createFor(
        elementClassOf((Class<? extends AtomicVector>) inspector.getLowestCommonType()),
        inspector.getTotalLength());
    int resultLength = 0;

    for(SEXP exp : inspector.getAllExpressions()) {
      AtomicAccessor accessor = AtomicAccessors.create(exp,
          elementClassOf((Class<AtomicVector>) inspector.getLowestCommonType()));
      for(int i=0;i!=accessor.length();++i) {
        if(accessor.isNA(i)) {
          builder.setNA(resultLength++);
        } else {
          builder.set(resultLength++, accessor.get(i));
        }
      }
    }
    return builder.build();
  }

  private static SEXP combineToList(Inspector inspector) {
    ListVector.Builder list = new ListVector.Builder();
    for(SEXP exp : inspector.getAllExpressions()) {
      for(SEXP element : exp.elements()) {
        list.add(element);
      }
    }
    return list.build();
  }

  /**
   * Finds the common type of an expression
   */
  static class Inspector extends SexpVisitor {

    private int totalLength;
    private List<SEXP> allExpressions = new ArrayList<SEXP>();
    private Class resultClass = LogicalVector.class;

    /**
     * Visits each element of {@code ListExp}
     */
    Inspector(PairList listExp) {
      for(SEXP exp : listExp) {
        exp.accept(this);
      }
    }

    @Override
    public void visit(DoubleVector realExp) {
      maybeWidenType(realExp);
      add(realExp);
    }

    @Override
    public void visit(IntVector intExp) {
      maybeWidenType(intExp);
      add(intExp);
    }

    @Override
    public void visit(LogicalVector logicalExp) {
      add(logicalExp);
    }

    @Override
    public void visit(NullExp nilExp) {
      // ignore
    }

    @Override
    public void visit(StringVector stringExp) {
      maybeWidenType(stringExp);
      add(stringExp);
    }

    @Override
    public void visit(ListVector listExp) {
      maybeWidenType(listExp);
      for(SEXP exp : listExp) {
        add(exp);
      }
    }

    @Override
    protected void unhandled(SEXP exp) {
      maybeWidenType(exp);
      add(exp);
    }

    private void add(SEXP exp) {
      allExpressions.add(exp);
      totalLength += exp.length();
    }

    private void maybeWidenType(SEXP y) {
      if(resultClass == ListVector.class) {
        // already as wide as we're going to get
        return;
      }

      if(!(y instanceof AtomicVector)) {
        resultClass = ListVector.class;

      } else if(resultClass == LogicalVector.class) {
        // everything is wider than logical
        resultClass = y.getClass();

      } else if(resultClass == IntVector.class) {
        if( !(y instanceof IntVector)) {
          resultClass = y.getClass();
        }

      } else if(resultClass == Double.class) {
        if( y instanceof StringVector) {
          resultClass = y.getClass();
        }
      }
    }

    /**
     * @return the common type of the visited expressions
     */
    public Class<? extends SEXP> getLowestCommonType() {
      return resultClass;
    }

    public int getTotalLength() {
      return totalLength;
    }

    public List<SEXP> getAllExpressions() {
      return allExpressions;
    }
  }
}
