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

import r.jvmi.annotations.ArgumentList;
import r.jvmi.annotations.NamedFlag;
import r.lang.*;
import r.lang.exception.EvalException;

public class Summary {



  public static SEXP min(@ArgumentList ListVector arguments,
                         @NamedFlag("na.rm") boolean removeNA) {

    return range(arguments, removeNA).getElementAsSEXP(0);

  }

  public static SEXP max(@ArgumentList ListVector arguments,
                         @NamedFlag("na.rm") boolean removeNA) {

    return range(arguments, removeNA).getElementAsSEXP(1);

  }


  public static SEXP range(@ArgumentList ListVector arguments,
                           @NamedFlag("na.rm") boolean removeNA) {

    Vector minValue = null;
    Vector maxValue = null;
    Vector.Type resultType = IntVector.VECTOR_TYPE;

    for(SEXP argument : arguments) {
      AtomicVector vector = EvalException.checkedCast(argument);

      if(vector.getVectorType().isWiderThan(resultType)) {
        resultType = vector.getVectorType();
      }

      for(int i=0;i!=vector.length();++i) {
        if(vector.isElementNA(i)) {
          if(!removeNA) {
            Vector.Builder result = resultType.newBuilder();
            result.addNA();
            result.addNA();
            return result.build();
          }
        } else {
          resultType = Vector.Type.widest(resultType, vector.getVectorType());

          if(maxValue == null || resultType.compareElements(maxValue, 0, vector, i) < 0) {
            maxValue = resultType.getElementAsVector(vector, i);
          }
          if(minValue == null || resultType.compareElements(minValue, 0, vector, i) > 0) {
            minValue = resultType.getElementAsVector(vector, i);
          }
        }
      }
    }
    if(maxValue == null) {
      return new DoubleVector(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    } else {
      Vector.Builder result = resultType.newBuilder();
      result.addFrom(minValue, 0);
      result.addFrom(maxValue, 0);
      return result.build();
    }
  }

  public static double prod(@ArgumentList ListVector arguments, @NamedFlag("na.rm") boolean removeNA) {
    double product = 1;
    for(SEXP argument : arguments) {
      AtomicVector vector = EvalException.checkedCast(argument);
      for(int i=0;i!=vector.length();++i) {
        if(vector.isElementNA(i)) {
          if(!removeNA) {
            return DoubleVector.NA;
          }
        } else {
          product = product * vector.getElementAsDouble(i);
        }
      }
    }
    return product;
  }

  public static SEXP sum(@ArgumentList ListVector arguments, @NamedFlag("na.rm") boolean removeNA) {
    int intSum = 0;
    double doubleSum = 0;
    boolean haveDouble = false;

    for(SEXP argument : arguments) {
      if(argument instanceof IntVector || argument instanceof LogicalVector) {
        AtomicVector vector = (AtomicVector)argument;
        for(int i=0;i!=argument.length();++i) {
          if(vector.isElementNA(i)) {
            if(!removeNA) {
              return haveDouble ? new DoubleVector(DoubleVector.NA) : new IntVector(IntVector.NA);
            }
          } else {
            intSum += vector.getElementAsInt(i);
          }
        }
      } else if(argument instanceof DoubleVector) {
        DoubleVector vector = (DoubleVector)argument;
        haveDouble = true;
        for(int i=0;i!=vector.length();++i) {
          if(vector.isElementNA(i)) {
            if(!removeNA) {
              return new DoubleVector(DoubleVector.NA);
            }
          } else {
            doubleSum += vector.getElementAsDouble(i);
          }
        }
      } else {
        throw new EvalException("invalid 'type' (" + argument.getTypeName() + ") of argument");
      }
    }
    return haveDouble ? new DoubleVector(doubleSum + intSum) : new IntVector(intSum);
  }

}
