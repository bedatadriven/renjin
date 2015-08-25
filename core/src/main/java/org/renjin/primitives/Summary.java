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

package org.renjin.primitives;

import java.io.IOException;

import com.google.common.math.IntMath;

import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Deferrable;
import org.renjin.invoke.annotations.GroupGeneric;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.NamedFlag;
import org.renjin.primitives.summary.DeferredMax;
import org.renjin.primitives.summary.DeferredMean;
import org.renjin.primitives.summary.DeferredMin;
import org.renjin.primitives.summary.DeferredSum;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.ComplexArrayVector;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.RawVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;


/**
 * Summary group functions of vectors such as min, max, sum, etc.
 */
public class Summary {

  private Summary() {}

  @Builtin
  @GroupGeneric
  public static SEXP min(@ArgumentList ListVector arguments,
                         @NamedFlag("na.rm") boolean removeNA) {

    if(arguments.length() == 1 && !removeNA) {
      return new DeferredMin((Vector) arguments.get(0), AttributeMap.EMPTY);
    }
    try {
      return new RangeCalculator()
              .setRemoveNA(removeNA)
              .addList(arguments)
              .getMinimum();
    } catch (RangeContainsNA e) {
      return new DoubleArrayVector(DoubleVector.NA);
    }
  }

  @Builtin
  @GroupGeneric
  public static SEXP max(@ArgumentList ListVector arguments,
                         @NamedFlag("na.rm") boolean removeNA) {
    if(arguments.length() == 1 && !removeNA) {
      return new DeferredMax((Vector) arguments.get(0), AttributeMap.EMPTY);
    }
    try {
      return new RangeCalculator()
              .setRemoveNA(removeNA)
              .addList(arguments)
              .getMaximum();
    } catch (RangeContainsNA e) {
      return new DoubleArrayVector(DoubleVector.NA);
    }
  }


  /**
   * range returns a vector containing the minimum and maximum of all the given arguments.
   * (And recurses through lists!)
   *
   * @param arguments  any numeric or character objects.
   * @param removeNA indicating if NA's should be omitted.
   * @return a vector containing the minimum and maximum of all the given arguments.
   */
  @Builtin
  @GroupGeneric
  public static Vector range(@ArgumentList ListVector arguments,
                             @NamedFlag("na.rm") boolean removeNA) {

    // in the C implementation, this primitive actually delegates back to a
    // function in the base library called "range.default". I don't think 
    // it's a good idea to create a circular dependency between the 
    // the primitives layer and the base library package, so we're implementing here.

    // another oddity: the min() and max() functions do not accept lists or 
    // other recursive structures. The range() implementation does.

    try {
      return new RangeCalculator()
              .setRemoveNA(removeNA)
              .setRecursive(true)
              .addList(arguments)
              .getRange();
    } catch (RangeContainsNA e) {
      return new DoubleArrayVector(DoubleVector.NA, DoubleVector.NA);
    }
  }

  private static class RangeContainsNA extends Exception {  }
  public static class RangeCalculator {
    private boolean removeNA;
    private boolean recursive;
    private Vector minValue = null;
    private Vector maxValue = null;
    private Vector.Type resultType = IntVector.VECTOR_TYPE;

    /**
     * It is tempting to immediately return once the first NA is encountered,
     * but in the CR, the return type is determined by ALL the elements
     * in the input, not just the ones before the first NA.
     */
    private boolean naEncountered = false;

    private boolean nanEncountered = false;

    public RangeCalculator setRemoveNA(boolean removeNA) {
      this.removeNA = removeNA;
      return this;
    }

    public RangeCalculator setRecursive(boolean recursive) {
      this.recursive = recursive;
      return this;
    }

    public RangeCalculator addList(ListVector list) throws RangeContainsNA {
      for(SEXP argument : list) {
        if(argument instanceof AtomicVector) {
          addVector(argument);
        } else if(recursive && argument instanceof ListVector) {
          addList((ListVector)argument);
        } else {
          throw new EvalException("invalid 'type' (%s) of argument", argument.getTypeName());
        }
      }
      return this;
    }

    private void addVector(SEXP argument) throws RangeContainsNA {
      AtomicVector vector = EvalException.checkedCast(argument);

      if(vector instanceof ComplexVector) {
        throw new EvalException("invalid 'type' (complex) of argument");
      }

      if(vector.getVectorType().isWiderThan(resultType)) {
        resultType = vector.getVectorType();
      }
      if (vector instanceof DoubleArrayVector) {
        double maxDblValue = Double.NEGATIVE_INFINITY;
        double minDblValue = Double.POSITIVE_INFINITY;
        double[] values = ((DoubleArrayVector) vector).toDoubleArrayUnsafe();
       for(int i=0;i<values.length;i++) {
         if (DoubleVector.isNA(values[i])) {
           if(!removeNA) {
             naEncountered = true;
           }
          } else {
           if (values[i] > maxDblValue) {
             maxDblValue = values[i];
           }
           if (values[i] < minDblValue) {
             minDblValue = values[i];
           }
         }
       }
       resultType = DoubleVector.VECTOR_TYPE;
       maxValue = DoubleVector.valueOf(maxDblValue);
       minValue = DoubleVector.valueOf(minDblValue);
       return;
      }
 
      for(int i=0;i!=vector.length();++i) {
        if(vector.isElementNA(i)) {
          if (!removeNA) {
            naEncountered = true;
          }
        } else if(isNaN(vector, i)) {
          if( !removeNA) {
            nanEncountered = true;
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

    private boolean isNaN(AtomicVector vector, int i) {
      if(vector instanceof DoubleVector) {
        double x = vector.getElementAsDouble(i);
        if(Double.isNaN(x)) {
          return true;
        }
      }
      return false;
    }

    public Vector getRange() {
      if(maxValue == null) {
        return new DoubleArrayVector(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
      } else if(nanEncountered) {
        return new DoubleArrayVector(Double.NaN, Double.NaN);
      } else {
        Vector.Builder result = resultType.newBuilder();
        if(naEncountered) {
          result.addNA();
          result.addNA();
        } else {
          result.addFrom(minValue, 0);
          result.addFrom(maxValue, 0);
        }
        return result.build();
      }
    }

    public Vector getMinimum() {
      return getRange().getElementAsSEXP(0);
    }

    public Vector getMaximum() {
      return getRange().getElementAsSEXP(1);
    }

    private Vector buildNA() {
      return resultType.newBuilder().addNA().build();
    }
  }

  /**
   *  returns the product of all the values present in its arguments.
   *
   * @param arguments
   * @param removeNA
   * @return
   */
  @Builtin
  @GroupGeneric
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

  @Builtin
  @GroupGeneric
  public static SEXP sum(@Current Context context, @ArgumentList ListVector arguments,
                         @NamedFlag("na.rm") boolean removeNA) throws IOException {
    double realSum = 0;
    boolean haveDouble = false;
    double imaginarySum = 0;
    boolean haveComplex = false;

    if(arguments.length() == 1 && !removeNA &&
        (arguments.get(0) instanceof DoubleVector || arguments.get(0) instanceof DeferredComputation)) {
      return new DeferredSum((Vector) arguments.get(0), AttributeMap.EMPTY);
    }

    for(SEXP argument : arguments) {
      if(argument instanceof IntVector || argument instanceof LogicalVector) {
        AtomicVector vector = (AtomicVector)argument;
        for(int i=0;i!=argument.length();++i) {
          if(vector.isElementNA(i)) {
            if(!removeNA) {
              return haveDouble ? new DoubleArrayVector(DoubleVector.NA) : new IntArrayVector(IntVector.NA);
            }
          } else {
            int element = vector.getElementAsInt(i);
            realSum += element;
          }
        }
      } else if(argument instanceof DoubleVector) {
        DoubleVector vector = (DoubleVector)argument;
        haveDouble = true;
        for(int i=0;i!=vector.length();++i) {
          if(vector.isElementNA(i)) {
            if(!removeNA) {
              return new DoubleArrayVector(DoubleVector.NA);
            }
          } else {
            realSum += vector.getElementAsDouble(i);
          }
        }
      } else if(argument instanceof ComplexVector) {
        ComplexVector vector = (ComplexVector)argument;
        haveComplex = true;
        for(int i=0;i!=vector.length();++i) {
          if(vector.isElementNA(i)) {
            if(!removeNA) {
              return new ComplexArrayVector(ComplexVector.NA);
            }
          } else {
            Complex z = vector.getElementAsComplex(i);
            realSum += z.getReal();
            imaginarySum += z.getImaginary();
          }
        }

      } else {
        throw new EvalException("invalid 'type' (" + argument.getTypeName() + ") of argument");
      }
    }
    if(haveComplex) {
      return new ComplexArrayVector(new Complex(realSum, imaginarySum));

    } else if(haveDouble) {
      return new DoubleArrayVector(realSum);

    } else {
      if(realSum < Integer.MIN_VALUE || realSum > Integer.MAX_VALUE) {
        Warning.emitWarning(context, context.getCall(), false, "Integer overflow - use sum(as.numeric(.))");
        return new IntArrayVector(IntVector.NA);
      }
      return new IntArrayVector((int)realSum);
    }
  }

  /**
   * Given a set of logical vectors, is at least one of the values true?
   *
   * Coercion of types other than integer (raw, double, complex, character, list) gives a warning
   * as this is often unintentional
   *
   * @param arguments zero or more logical vectors. Other objects of zero length are ignored,
   *    and the rest are coerced to logical ignoring any class
   * @param removeNA   If true NA values are removed before the result is computed.
   * @return  Let x denote the concatenation of all the logical vectors in ...
   *   (after coercion), after removing NAs if requested by na.rm = TRUE.
   * <p>TRUE if at least one of the values in x is TRUE, and FALSE if all of the values in x are FALSE
   * (including if there are no values). Otherwise the value is NA (which can only occur if na.rm = FALSE
   * and ... contains no TRUE values and at least one NA value).
   */
  @Builtin
  @GroupGeneric
  public static Logical any(@ArgumentList ListVector arguments,
                            @NamedFlag("na.rm") boolean removeNA) {

    for(SEXP argument : arguments) {
      Vector vector = (Vector) argument;
      for(int i=0;i!=vector.length();++i) {
        if(vector.isElementNA(i)) {
          if(!removeNA) {
            return Logical.NA;
          }
        } else if(vector.getElementAsDouble(i) != 0) {
          return Logical.TRUE;
        }
      }
    }
    return Logical.FALSE;
  }

  /**
   * Given a set of logical vectors, are all of the values true?
   *
   * @param arguments zero or more logical vectors. Other objects of zero length are ignored, and the rest
   *  are coerced to logical ignoring any class.
   * @param removeNA  If true NA values are removed before the result is computed.
   * @return Let x denote the concatenation of all the logical vectors in ... (after coercion), after removing NAs if
   *  requested by na.rm = TRUE.
   * <p>The value returned is TRUE if all of the values in x are TRUE (including if there are no values), and
   * FALSE if at least one of the values in x is FALSE. Otherwise the value is NA (which can only occur if
   *  na.rm = FALSE and ... contains no FALSE values and at least one NA value).
   */
  @Builtin
  @GroupGeneric
  public static Logical all(@ArgumentList ListVector arguments,
                            @NamedFlag("na.rm") boolean removeNA) {

    for(SEXP argument : arguments) {
      Vector vector = (Vector) argument;
      for(int i=0;i!=vector.length();++i) {
        if(vector.isElementNA(i)) {
          if(!removeNA) {
            return Logical.NA;
          }
        } else if(vector.getElementAsDouble(i) == 0) {
          return Logical.FALSE;
        }
      }
    }
    return Logical.TRUE;
  }


  @Deferrable
  @Internal
  public static DoubleVector mean(Vector x) {

    if(x instanceof DeferredComputation || x.length() > 100000) {
      return new DeferredMean(x, AttributeMap.EMPTY);
    }

    double mean = 0;
    for (int i=0;i<x.length();i++){
      mean+=x.getElementAsDouble(i);
    }
    return new DoubleArrayVector(mean / x.length());
  }

  public double[] compute(Vector[] x) {
    double[] x_array = ((DoubleArrayVector)x[0]).toDoubleArrayUnsafe();
    double sum = 0;
    for(int i=0;i!=x_array.length;++i) {
      sum += x_array[i];
    }
    return new double[] { sum / x_array.length };
  }

  @Builtin
  public static DoubleVector cumsum(DoubleVector source) {
    DoubleArrayVector.Builder result = new DoubleArrayVector.Builder(source.length());
    double sum = 0;
    for (int i = 0; i < source.length(); i++) {
      sum += source.getElementAsDouble(i);
      if (Double.isNaN(sum)) {
        break;
      } 
      result.set(i, sum);
    }
    return result.build();
  }
  
  @Builtin
  public static DoubleVector cumsum(RawVector source) {
    return cumsum(Vectors.asDouble(source));
  }

  @Builtin
  public static IntVector cumsum(@Current Context context, IntVector source) {
    return cumulativeSumIntegers(source);
  }
  
  @Builtin
  public static ComplexVector cumsum(ComplexVector source) {
    ComplexArrayVector.Builder result = new ComplexArrayVector.Builder(source.length());
    Complex sum = new Complex(0, 0);
    for (int i = 0; i < source.length(); i++) {
      if(source.isElementNA(i)) {
        break;
      }
      sum = sum.add(source.getElementAsComplex(i));
      result.set(i, sum);
    }
    return result.build();
  }
  
  @Builtin
  public static IntVector cumsum(@Current Context context, LogicalVector source) {
    return cumulativeSumIntegers(source);
  }
  
  @Builtin 
  public static DoubleVector cumsum(StringVector source) {
    return cumsum(Vectors.asDouble(source));
  }

  private static IntVector cumulativeSumIntegers(Vector source) {
    IntArrayVector.Builder result = new IntArrayVector.Builder(source.length());
    int sum = 0;
    for (int i = 0; i < source.length(); i++) {
      int x = source.getElementAsInt(i);
      if(x == IntVector.NA) {
        // remaining elements are initialized to NA
        break;
      }
      try {
        sum = IntMath.checkedAdd(sum, x);
      } catch (ArithmeticException e) {
//        context.warn("integer overflow in 'cumsum'; use 'cumsum(as.numeric(.))");
        break;
      }
      result.set(i, sum);
    }
    return (result.build());
  }

  @Builtin
  public static DoubleVector cumprod(Vector source) {
    DoubleArrayVector.Builder result = new DoubleArrayVector.Builder();
    double sum = source.getElementAsDouble(0);
    result.add(sum);
    for (int i = 1; i < source.length(); i++) {
      sum *= source.getElementAsDouble(i);
      if (Double.isNaN(sum)) {
        result.addNA();
      } else {
        result.add(sum);
      }
    }
    return (result.build());
  }

  @Builtin
  public static DoubleVector cummax(Vector source) {
    DoubleArrayVector.Builder result = new DoubleArrayVector.Builder();
    double max = source.getElementAsDouble(0);
    result.add(max);
    for (int i = 1; i < source.length(); i++) {
      if (source.getElementAsDouble(i) > max || source.isElementNA(i)) {
        max = source.getElementAsDouble(i);
      } else if (DoubleVector.isNaN(source.getElementAsDouble(i))) {
        max = DoubleVector.NA;
      }
      result.add(max);
    }
    return (result.build());
  }

  @Builtin
  public static DoubleVector cummin(Vector source) {
    DoubleArrayVector.Builder result = new DoubleArrayVector.Builder();
    double min = source.getElementAsDouble(0);
    result.add(min);
    for (int i = 1; i < source.length(); i++) {
      if (source.getElementAsDouble(i) < min || source.isElementNA(i)) {
        min = source.getElementAsDouble(i);
      } else if (DoubleVector.isNaN(source.getElementAsDouble(i))) {
        min = DoubleVector.NA;
      }
      result.add(min);
    }
    return (result.build());
  }

  @Internal
  public static Vector pmin(boolean naRm, @ArgumentList ListVector vectors) {
    ParallelProcessor processor = new ParallelProcessor(naRm, vectors) {
      @Override
      boolean predicate(Number x, Number y) {
        return ((Comparable)x).compareTo(y) > 0;
      }
    };
    return processor.compute();
  }

  @Internal
  public static Vector pmax(boolean naRm, @ArgumentList ListVector vectors) {
    ParallelProcessor processor = new ParallelProcessor(naRm, vectors) {
      @Override
      boolean predicate(Number x, Number y) {
        return ((Comparable)x).compareTo(y) < 0;
      }
    };
    return processor.compute();
  }


  private abstract static class ParallelProcessor {

    private ListVector arguments;
    private int resultLength;
    private boolean realResult;
    private boolean naRm;

    public ParallelProcessor(boolean naRm, ListVector arguments) {
      this.arguments = arguments;
      this.naRm = naRm;
      if(arguments.length() == 0) {
        throw new EvalException("no arguments");
      }
      this.resultLength = arguments.maxElementLength();
    }


    private void validateArguments() {
      // validate arguments and determine result type
      for(SEXP argument : arguments) {
        if(argument.length() == 0) {
          throw new EvalException("cannot mix 0-length vectors with others");
        } else if(argument instanceof DoubleVector) {
          realResult = true;
        } else if(! (argument instanceof LogicalVector || argument instanceof IntVector) ) {
          throw new EvalException("cannot handle argument of type '%s'", argument.getTypeName());
        }
      }
    }

    public Vector compute() {
      if(resultLength == 0) {
        return Null.INSTANCE;
      } else {
        validateArguments();
        Vector.Builder builder = createBuilder();
        for(int i=0;i!=resultLength;++i) {
          Number result = computeResult(i);
          if(result == null) {
            builder.addNA();
          } else {
            builder.add(result);
          }
        }
        return builder.build();
      }
    }

    private Number computeResult(int resultIndex) {
      Number result = null;
      for(int argIndex=0;argIndex<arguments.length();++argIndex) {
        Number value = getValue(argIndex, resultIndex);
        if(value == null && !naRm) {
          return null;
        } else if(result == null && value != null) {
          result = value;
        } else if(result != null && value != null &&
                predicate(result, value)) {
          result = value;
        }
      }
      return result;
    }

    private Vector.Builder createBuilder() {
      if(realResult) {
        return new DoubleArrayVector.Builder();
      } else {
        return new IntArrayVector.Builder();
      }
    }

    private Vector getVector(int index) {
      return (Vector)arguments.getElementAsSEXP(index);
    }

    private Number getValue(int argument, int index) {
      Vector vector = getVector(argument);
      int vectorIndex = index % vector.length();
      if(vector.isElementNA(vectorIndex)) {
        return null;
      } else {
        if(realResult) {
          return vector.getElementAsDouble(vectorIndex);
        } else {
          return vector.getElementAsInt(vectorIndex);
        }
      }
    }

    abstract boolean predicate(Number x, Number y);
  }


}
