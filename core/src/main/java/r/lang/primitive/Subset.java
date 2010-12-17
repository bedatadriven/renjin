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
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.Evaluate;
import r.lang.primitive.annotations.Indices;
import r.lang.primitive.annotations.Primitive;


public class Subset {

  @Primitive("$")
  public static SEXP getElementByName(PairList list, @Evaluate(false) SymbolExp symbol) {
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

  @Primitive("$")
  public static SEXP getElementByName(Environment env, @Evaluate(false) SymbolExp symbol) {
    SEXP value = env.getVariable(symbol);
    if(value == SymbolExp.UNBOUND_VALUE) {
      return Null.INSTANCE;
    }
    return value;
  }

  @Primitive("$")
  public static SEXP getElementByName(ListVector list, @Evaluate(false) SymbolExp name) {
    SEXP match = null;
    int matchCount = 0;

    for(int i=0;i!=list.length(); ++i) {
      if(list.getName(i).startsWith(name.getPrintName())) {
        match = list.get(i);
        matchCount++;
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }

  @Primitive("$<-")
  public static SEXP setElementByName(ListVector list, @Evaluate(false) SymbolExp name, SEXP value) {
    ListVector.Builder result = ListVector.buildFromClone(list);

    int index = list.getIndexByName(name);
    if(index == -1) {
      result.add(name, value);
    } else {
      result.set(index, value);
    }
    return result.build();
  }

  @Primitive("[")
  public static SEXP getSubset(Vector vector, LogicalVector include) {
    Vector.Builder result = vector.newBuilder(0);
    int resultLen = 0;
    for(int i=0;i!=vector.length();++i) {
      int b = include.getElementAsInt(i % include.length());
      if( b == LogicalVector.NA ) {
        result.setNA(i);
      } else if( b != 0) {
        result.setFrom(resultLen++, vector, i);
      }
    }
    return result.build();
  }

  @Primitive("[")
  public static SEXP getSubset(Vector vector, @Indices int indices[]) {
    Vector.Builder builder = vector.newBuilder(0);
    int resultLen = 0;

    if(arePositions(indices)) {

      for(int index : indices) {
        if(index > vector.length()) {
          builder.setNA(resultLen++);
        } else if(index > 0) {
          builder.setFrom(resultLen++, vector, index-1);
        }
      }
      return builder.build();

    } else {

      /* Negative indices indicate elements that should not be
         returned.

         For example, -1, means don't include the first element.
       */

      boolean excluded[] = toMask(indices, vector.length());
      for(int i=0;i!=vector.length();++i) {
        if(!excluded[i]) {
          builder.setFrom(resultLen++, vector, i);
        }
      }
    }

    return builder.build();
  }

  @Primitive("[[")
  public static SEXP getSingleElement(Vector vector, @Indices int index) {
    if(index < 0) {
      throw new EvalException("attempt to select more than one element");
    } else if(index == 0) {
      throw new EvalException("attempt to select less than one element");
    }

    if(index <= vector.length()) {
      return vector.getElementAsSEXP(index-1);
    } else {
      throw new EvalException("subscript out of bounds");
    }
  }

  @Primitive("[<-")
  public static SEXP setSubset(Vector target, @Indices int indices[], Vector values) {
    if(indices.length % values.length() != 0) {
      throw new EvalException("number of items to replace is not a multiple of replacement length");
    }

    Vector.Builder result = copyWideningIfNecessary(target, values);

    for(int i=0;i!=indices.length;++i) {
      int index = indices[i];
      if(index > 0) {
        result.setFrom(index-1, values, i % values.length());
      }
    }
    return result.build();
  }

  @Primitive("[<-")
  public static SEXP setSubset(Vector target, LogicalVector subscripts, Vector values) {
    int replaceCount = 0;
    for(int i=0;i!=target.length();++i) {
      int subscriptIndex = i % subscripts.length();
      if(subscripts.isElementNA(subscriptIndex)) {
        throw new EvalException("NAs are not allowed in subscripted assignments");
      }
      if(subscripts.getElementAsInt(subscriptIndex) != 0) {
        replaceCount++;
      }
    }

    if(replaceCount % values.length() != 0) {
      throw new EvalException("number of items to replace is not a multiple of replacement length");
    }

    Vector.Builder result = copyWideningIfNecessary(target, values);
    int replacedCount = 0;
    for(int i=0;i!=target.length();++i) {
      int subscriptIndex = i % subscripts.length();
      if(subscripts.getElementAsInt(subscriptIndex) != 0) {
        int valueIndex = replacedCount % values.length();
        result.setFrom(i, values, valueIndex);
        replacedCount++;
      }
    }

    return result.build();
  }

  private static Vector.Builder copyWideningIfNecessary(Vector toCopy, Vector otherElements) {
    Vector.Builder result;

    if(toCopy.isWiderThan(otherElements)) {
      result = toCopy.newCopyBuilder();
    } else {
      result = otherElements.newBuilder(0);
      for(int i=0;i!= toCopy.length();++i) {
        result.setFrom(i, toCopy, i);
      }
    }
    return result;
  }

  @Primitive("[")
  public static SEXP getSubset(Vector vector, StringVector names) {
    Vector.Builder builder = vector.newBuilder(names.length());

    int resultLen = 0;
    for(String name : names) {
      int index = vector.getIndexByName(name);
      if(index == -1) {
        builder.setNA(resultLen++);
      } else {
        builder.setFrom(resultLen++, vector, index);
      }
    }
    return builder.build();
  }

  /**
   * @return  true if the indices are all zero or positive
   */
  private static boolean arePositions(int indices[]) {
    boolean hasNeg = false;
    boolean hasPos = false;

    for(int i=0;i!=indices.length;++i) {
      if(indices[i] < 0) {
        hasNeg = true;
      } else if(indices[i] > 0) {
        hasPos = true;
      }
    }
    if(hasNeg && hasPos) {
      throw new EvalException("only 0's may be mixed with negative subscripts");
    }
    return !hasNeg;
  }

  private static boolean[] toMask(int indices[], int vectorLength) {
    boolean mask[] = new boolean[vectorLength];
    for(int i=0;i!=indices.length;++i) {
      if(indices[i] != 0) {
        int index = (-indices[i]) - 1;
        if( index < vectorLength ) {
          mask[ index ] = true;
        }
      }
    }
    return mask;
  }
}
