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

package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.ArgumentList;
import org.renjin.primitives.annotations.DefaultValue;
import org.renjin.primitives.annotations.Evaluate;
import org.renjin.primitives.annotations.Generic;
import org.renjin.primitives.annotations.NamedFlag;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Environment;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Vector;


import com.google.common.base.Strings;

public class Subsetting {

  private Subsetting() {
  }

  @Generic
  @Primitive("$")
  public static SEXP getElementByName(PairList list,
      @Evaluate(false) Symbol symbol) {
    SEXP match = null;
    int matchCount = 0;

    for (PairList.Node node : list.nodes()) {
      if (node.hasTag()) {
        if (node.getTag().getPrintName().startsWith(symbol.getPrintName())) {
          match = node.getValue();
          matchCount++;
        }
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }

  @Generic
  @Primitive("$")
  public static SEXP getElementByName(Environment env,
      @Evaluate(false) Symbol symbol) {
    SEXP value = env.getVariable(symbol);
    if (value == Symbol.UNBOUND_VALUE) {
      return Null.INSTANCE;
    }
    return value;
  }

  @Generic
  @Primitive("$")
  public static SEXP getElementByName(ListVector list,
      @Evaluate(false) Symbol name) {
    SEXP match = null;
    int matchCount = 0;

    for (int i = 0; i != list.length(); ++i) {
      String elementName = list.getName(i);
      if (!StringVector.isNA(elementName)) {
        if (elementName.equals(name.getPrintName())) {
          return list.getElementAsSEXP(i);
        } else if (elementName.startsWith(name.getPrintName())) {
          match = list.get(i);
          matchCount++;
        }
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }

  @Generic
  @Primitive("$<-")
  public static SEXP setElementByName(ListVector list,
      @Evaluate(false) Symbol name, SEXP value) {
    ListVector.NamedBuilder result = ListVector.buildNamedFromClone(list);

    int index = list.getIndexByName(name.getPrintName());
    if (index == -1) {
      result.add(name, value);
    } else {
      result.set(index, value);
    }
    return result.build();
  }

  @Generic
  @Primitive("$<-")
  public static PairList setElementByName(PairList.Node pairList,
      @Evaluate(false) Symbol name, SEXP value) {
    PairList.Builder builder = new PairList.Builder();

    boolean found = false;

    for (PairList.Node node : pairList.nodes()) {
      if (node.getRawTag().equals(name)) {
        found = true;
        if (value != Null.INSTANCE) {
          builder.add(name, value);
        }
      } else {
        builder.add(node.getRawTag(), node.getValue());
      }
    }

    if (!found && value != Null.INSTANCE) {
      builder.add(name, value);
    }
    return builder.build();
  }

  @Generic
  @Primitive("$<-")
  public static SEXP setElementByName(Environment env,
      @Evaluate(false) Symbol name, SEXP value) {
    env.setVariable(name, value);
    return env;
  }

  /**
   * Same as "[" but not generic
   */
  @Primitive(".subset")
  public static SEXP subset(Vector source, @ArgumentList ListVector arguments,
      @NamedFlag("drop") @DefaultValue(true) boolean drop) {
    return getSubset(source, arguments, drop);
  }

  @Primitive(".subset")
  public static SEXP subset(PairList source, @ArgumentList ListVector arguments,
      @NamedFlag("drop") @DefaultValue(true) boolean drop) {
    return getSubset(source.toVector(), arguments, drop);
  }
  
  @Generic
  @Primitive("[")
  public static SEXP getSubset(SEXP source, @ArgumentList ListVector arguments,
      @NamedFlag("drop") @DefaultValue(true) boolean drop) {

    // handle an exceptional case: if source is NULL,
    // the result is always null
    if (source == Null.INSTANCE) {
      return source;
    }

    // handle the most common case first quickly:
    // x[ i ], where i > 0
    if (drop == true && source instanceof AtomicVector
        && arguments.length() == 1) {
      SEXP subscript = arguments.getElementAsSEXP(0);
      if (subscript.length() == 1
          && (subscript instanceof DoubleVector || subscript instanceof IntVector)) {
        int index = ((AtomicVector) subscript).getElementAsInt(0);
        if (index > 0) {
          if (index <= source.length()) {
            return source.getElementAsSEXP(index - 1);
          } else {
            Vector.Builder result = ((Vector) source)
                .newBuilderWithInitialSize(1);
            result.setNA(0);
            return result.build();
          }
        }
      }
    }

    // handle the more complicated cases
    return new SubscriptOperation()
        .setSource(source, arguments)
        .setDrop(drop)
        .extract();
  }

  
  @Generic
  @Primitive("[<-")
  public static SEXP setSubset(SEXP source, @ArgumentList ListVector arguments) {
    return new SubscriptOperation()
        .setSource(source, arguments, 0, 1)
        .replace((Vector) arguments.getElementAsSEXP(arguments.length() - 1),
            false);
  }

  @Generic
  @Primitive("[[<-")
  public static SEXP setSingleElement(SEXP source,
      @ArgumentList ListVector arguments) {
    
    if(source instanceof Environment) {
      if(arguments.length() != 2) {
        throw new EvalException("Incorrect number of subscripts");
      }
      if(!(arguments.get(0) instanceof StringVector)) {
        throw new EvalException("wrong arguments for subsetting an environment");
      }
      String name = arguments.getElementAsString(0);
      SEXP value = arguments.getElementAsSEXP(1);
      ((Environment)source).setVariable(name, value);
      return source;
      
    } else if(source instanceof PairList || source instanceof Vector) {

      Vector result = new SubscriptOperation()
          .setSource(source, arguments, 0, 1)
          .replace(arguments.getElementAsSEXP(arguments.length() - 1), true);
      
      if(source instanceof PairList.Node) {
        return PairList.Node.fromVector(result);
      } else {
        return result;
      }
    } else {
      throw new EvalException("'%s' is not subsettable", source.getTypeName());
    } 
  }

  @Generic
  @Primitive("[[")
  public static SEXP getSingleElement(Vector vector, int index) {
    if (vector.length() == 0) {
      return Null.INSTANCE;
    }

    EvalException.check(index >= 0, "attempt to select more than one element");
    EvalException.check(index != 0, "attempt to select less than one element");
    EvalException.check(index <= vector.length(), "subscript out of bounds");

    return vector.getElementAsSEXP(index - 1);
  }

  @Generic
  @Primitive("[[")
  public static SEXP getSingleElement(PairList.Node pairlist, int index) {
    if (index > pairlist.length()) {
      throw new EvalException("subscript out of bounds");
    } else {
      return pairlist.getElementAsSEXP(index - 1);
    }
  }

  /**
   * Same as [[ but not marked as @Generic
   */
  @Primitive(".subset2")
  public static SEXP getSingleElementDefault(Vector vector, int index) {
    return getSingleElement(vector, index);
  }

  @Primitive(".subset2")
  public static SEXP getSingleElementDefaultByExactName(Vector vector,
      String name) {
    return getSingleElementByExactName(vector, name);
  }

  @Generic
  @Primitive("[[")
  public static SEXP getSingleElementByExactName(Vector vector, String subscript) {
    int index = vector.getIndexByName(subscript);
    return index == -1 ? Null.INSTANCE : vector.getElementAsSEXP(index);
  }

  @Generic
  @Primitive("[[")
  public static SEXP getSingleElementByExactName(PairList.Node pairlist,
      String subscript) {
    return getSingleElementByExactName(pairlist.toVector(), subscript);
  }

  @Generic
  @Primitive("[[")
  public static SEXP getSingleElementByExactName(Environment env,
      String subscript) {
    SEXP value = env.getVariable(subscript);
    if (value == Symbol.UNBOUND_VALUE)
      return Null.INSTANCE;
    return value;
  }

  @Generic
  @Primitive("[[")
  public static SEXP getSingleElementByName(Vector vector, String subscript,
      boolean exact) {
    if (exact) {
      return getSingleElementByExactName(vector, subscript);
    } else {
      int matchCount = 0;
      SEXP match = Null.INSTANCE;

      for (int i = 0; i != vector.length(); ++i) {
        if (Strings.nullToEmpty(vector.getName(i)).startsWith(subscript)) {
          match = vector.getElementAsSEXP(i);
          matchCount++;
        }
      }

      return matchCount == 1 ? match : Null.INSTANCE;
    }
  }

  @Generic
  @Primitive("[[")
  public static SEXP getSingleElementByName(PairList.Node pairlist,
      String subscript, boolean exact) {
    return getSingleElementByName(pairlist.toVector(), subscript, exact);
  }
}
