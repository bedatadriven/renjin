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

import com.google.common.collect.Iterables;
import r.lang.*;
import r.lang.primitive.annotations.ArgumentList;
import r.lang.primitive.binding.AtomicAccessor;
import r.lang.primitive.binding.AtomicAccessors;
import r.lang.primitive.binding.AtomicBuilder;
import r.lang.primitive.binding.AtomicBuilders;

import java.util.ArrayList;
import java.util.List;

import static r.lang.primitive.binding.AtomicAccessors.elementClassOf;

public class Combine {


  public static SEXP combine(@ArgumentList PairList argList) {

    if(argList.length() == 0) {
      return NullExp.INSTANCE;
    }

    Inspector inspector = new Inspector((PairListExp) argList);

    Class<? extends SEXP> lowestCommonType = inspector.getLowestCommonType();
    if(AtomicExp.class.isAssignableFrom(lowestCommonType)) {
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
        elementClassOf((Class<? extends AtomicExp>) inspector.getLowestCommonType()),
        inspector.getTotalLength());
    int resultLength = 0;

    for(SEXP exp : inspector.getAllExpressions()) {
      AtomicAccessor accessor = AtomicAccessors.create(exp,
          elementClassOf((Class<AtomicExp>) inspector.getLowestCommonType()));
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
    List<SEXP> items = new ArrayList<SEXP>();

    for(SEXP sexp : inspector.getAllExpressions()) {
      if(sexp instanceof NullExp) {
        items.add(sexp);
      } else if(sexp instanceof AtomicExp) {
        AtomicAccessor accessor = AtomicAccessors.create(sexp);
        for(int i=0; i!=accessor.length();++i) {
          items.add( SEXPFactory.fromJava(accessor.get(i)) );
        }                        

      } else if(sexp instanceof ListExp) {
        Iterables.addAll(items, (ListExp) sexp);

      } else {
        items.add(sexp);
      }
    }
    return new ListExp(items);
  }

  /**
   * Finds the common type of an expression
   */
  static class Inspector extends SexpVisitor {

    private int totalLength;
    private List<SEXP> allExpressions = new ArrayList<SEXP>();
    private Class resultClass = LogicalExp.class;

    /**
     * Visits each element of {@code ListExp}
     */
    Inspector(PairList listExp) {
      for(SEXP exp : listExp) {
        exp.accept(this);
      }
    }

    @Override
    public void visit(DoubleExp realExp) {
      maybeWidenType(realExp);
      add(realExp);
    }

    @Override
    public void visit(IntExp intExp) {
      maybeWidenType(intExp);
      add(intExp);
    }

    @Override
    public void visit(LogicalExp logicalExp) {
      add(logicalExp);
    }

    @Override
    public void visit(NullExp nilExp) {
      // ignore
    }

    @Override
    public void visit(StringExp stringExp) {
      maybeWidenType(stringExp);
      add(stringExp);
    }

    @Override
    public void visit(ListExp listExp) {
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
      if(resultClass == ListExp.class) {
        // already as wide as we're going to get
        return;
      }

      if(!(y instanceof AtomicExp)) {
        resultClass = ListExp.class;

      } else if(resultClass == LogicalExp.class) {
        // everything is wider than logical
        resultClass = y.getClass();

      } else if(resultClass == IntExp.class) {
        if( !(y instanceof IntExp)) {
          resultClass = y.getClass();
        }

      } else if(resultClass == Double.class) {
        if( y instanceof StringExp ) {
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

    public boolean isLowestCommonTypeAtomic() {
      return AtomicExp.isAtomic(resultClass);
    }

    public int getTotalLength() {
      return totalLength;
    }

    public List<SEXP> getAllExpressions() {
      return allExpressions;
    }
  }
}
