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

package r.base;

import r.base.subscripts.SubscriptOperation;
import r.jvmi.annotations.*;
import r.lang.*;
import r.lang.exception.EvalException;


public class Subscript {

  @Generic @Primitive("$")
  public static SEXP getElementByName(PairList list, @Evaluate(false) Symbol symbol) {
    SEXP match = null;
    int matchCount = 0;

    for(PairList.Node node : list.nodes()) {
      if(node.hasTag()) {
        if(node.getTag().getPrintName().startsWith(symbol.getPrintName())) {
          match = node.getValue();
          matchCount++;
        }
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }

  @Generic @Primitive("$")
  public static SEXP getElementByName(Environment env, @Evaluate(false) Symbol symbol) {
    SEXP value = env.getVariable(symbol);
    if(value == Symbol.UNBOUND_VALUE) {
      return Null.INSTANCE;
    }
    return value;
  }

  @Generic @Primitive("$")
  public static SEXP getElementByName(ListVector list, @Evaluate(false) Symbol name) {
    SEXP match = null;
    int matchCount = 0;

    for(int i=0;i!=list.length(); ++i) {
      String elementName = list.getName(i);
      if(elementName.equals(name.getPrintName())) {
        return list.getElementAsSEXP(i);
      } else if(elementName.startsWith(name.getPrintName())) {
        match = list.get(i);
        matchCount++;
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }

  @Generic @Primitive("$<-")
  public static SEXP setElementByName(ListVector list, @Evaluate(false) Symbol name, SEXP value) {
    ListVector.Builder result = ListVector.buildFromClone(list);

    int index = list.getIndexByName(name.getPrintName());
    if(index == -1) {
      result.add(name, value);
    } else {
      result.set(index, value);
    }
    return result.build();
  }

  @Generic @Primitive("[")
  public static SEXP getSubset(@ArgumentList ListVector arguments,
                               @NamedFlag("drop") @DefaultValue(true) boolean drop) {
    return new SubscriptOperation()
        .setSource(arguments.getElementAsSEXP(0))
        .setDrop(drop)
        .setSubscripts(arguments, 1, 0)
        .extract();
  }

  @Generic @Primitive("[<-")
  public static SEXP setSubset(@ArgumentList ListVector arguments) {
    return new SubscriptOperation()
        .setSource(arguments.getElementAsSEXP(0))
        .setSubscripts(arguments, 1, 1)
        .replace((Vector) arguments.getElementAsSEXP(arguments.length()-1), false);
  }

  @Generic @Primitive("[[<-")
  public static SEXP setSingleElement(@ArgumentList ListVector arguments) {
    return new SubscriptOperation()
        .setSource(arguments.getElementAsSEXP(0))
        .setSubscripts(arguments, 1, 1)
        .replace(arguments.getElementAsSEXP(arguments.length()-1), true);
  }


  @Generic @Primitive("[[")
  public static SEXP getSingleElement(Vector vector, int index) {
    if(vector.length() == 0) {
      return Null.INSTANCE;
    }

    EvalException.check(index >= 0, "attempt to select more than one element");
    EvalException.check(index != 0, "attempt to select less than one element");
    EvalException.check(index <= vector.length(), "subscript out of bounds" );

    return vector.getElementAsSEXP(index-1);
  }

  @Generic @Primitive("[[")
  public static SEXP getSingleElement(PairList.Node pairlist, int index) {
    if(index > pairlist.length()) {
      throw new EvalException("subscript out of bounds");
    } else {
      return pairlist.getElementAsSEXP(index-1);
    }
  }

  /**
   * Same as [[ but not marked as @Generic
   */
  @Primitive(".subset2")
  public static SEXP getSingleElementDefault(Vector vector, int index) {
    return getSingleElement(vector, index);
  }


  @Generic @Primitive("[[")
  public static SEXP getSingleElementByExactName(Vector vector, String subscript) {
    int index = vector.getIndexByName(subscript);
    return index == -1 ? Null.INSTANCE : vector.getElementAsSEXP(index);
  }

  @Generic @Primitive("[[")
  public static SEXP getSingleElementByExactName(PairList pairlist, String subscript) {
    return getSingleElementByExactName(pairlist.toVector(), subscript);
  }

  @Generic @Primitive("[[")
  public static SEXP getSingleElementByName(Vector vector, String subscript, boolean exact) {
    if(exact) {
      return getSingleElementByExactName(vector, subscript);
    } else {
      int matchCount = 0;
      SEXP match = Null.INSTANCE;

      for(int i=0;i!=vector.length();++i) {
        if(vector.getName(i).startsWith(subscript)) {
          match = vector.getElementAsSEXP(i);
          matchCount ++;
        }
      }

      return matchCount == 1 ? match : Null.INSTANCE;
    }
  }

  @Generic @Primitive("[[")
  public static SEXP getSingleElementByName(PairList pairlist, String subscript, boolean exact) {
    return getSingleElementByName(pairlist.toVector(), subscript, exact);
  }

}
