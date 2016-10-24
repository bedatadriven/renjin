/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives;

import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.parser.NumericLiterals;
import org.renjin.parser.StringLiterals;
import org.renjin.primitives.summary.DeferredMean;
import org.renjin.primitives.summary.DeferredSum;
import org.renjin.sexp.*;

import java.io.IOException;


/**
 * Summary group functions of vectors such as min, max, sum, etc.
 */
public class Summary {

  private Summary() {}

  @Builtin
  @GroupGeneric
  public static SEXP min(@ArgumentList ListVector arguments,
                         @NamedFlag("na.rm") boolean removeNA) {

    return new RangeCalculator()
            .setRemoveNA(removeNA)
            .addList(arguments)
            .getMinimum();
  }

  @Builtin
  @GroupGeneric
  public static SEXP max(@ArgumentList ListVector arguments,
                         @NamedFlag("na.rm") boolean removeNA) {

    return new RangeCalculator()
            .setRemoveNA(removeNA)
            .addList(arguments)
            .getMaximum();
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

    return new RangeCalculator()
            .setRemoveNA(removeNA)
            .setRecursive(true)
            .addList(arguments)
            .getRange();
  }

  private static class RangeCalculator {
    private boolean removeNA;
    private boolean recursive;

    private Vector.Type resultType = IntVector.VECTOR_TYPE;

    private double minDoubleValue = Double.POSITIVE_INFINITY;
    private double maxDoubleValue = Double.NEGATIVE_INFINITY;
    private int minIntValue = Integer.MAX_VALUE;
    private int maxIntValue = Integer.MIN_VALUE;
    private boolean hasValues;
    private String minStringValue;
    private String maxStringValue;


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

    public RangeCalculator addList(ListVector list)  {
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

    private void addVector(SEXP argument) {
      AtomicVector vector = EvalException.checkedCast(argument);

      if (vector instanceof ComplexVector) {
        throw new EvalException("invalid 'type' (complex) of argument");
      }

      if (vector instanceof StringVector) {
        addStringVector((StringVector)vector);

      } else if (vector instanceof DoubleVector) {
        addDoubleVector((DoubleVector) vector);

      } else {
        addIntVector(vector);
      }
    }
    private void addIntVector(AtomicVector vector) {

      // An integer vector will never trigger a promotion of the
      // result type, so no need to continue if we've encountered an NA
      if(naEncountered) {
        return;
      }

      // Find the min/max of this vector FIRST,
      // before converting the type and THEN compare to the
      // extrema of the other vectors

      int minValue = Integer.MAX_VALUE;
      int maxValue = Integer.MIN_VALUE;

      boolean hasValues = false;

      for (int i = 0; i < vector.length(); i++) {
        int value = vector.getElementAsInt(i);

        if(IntVector.isNA(value)) {
          if(!removeNA) {
            // If we encounter an NA and na.rm = FALSE,
            // we can stop processing immediately.
            naEncountered = true;
            return;
          }
        } else {
          hasValues = true;
          minValue = Math.min(minValue, value);
          maxValue = Math.max(maxValue, value);
        }
      }

      if(hasValues) {
        if(resultType == StringVector.VECTOR_TYPE) {
          addStringRange(Integer.toString(minValue), Integer.toString(maxValue));
        } else {
          this.hasValues = true;
          minIntValue = Math.min(minIntValue, minValue);
          maxIntValue = Math.max(maxIntValue, maxValue);
          minDoubleValue = Math.min(minDoubleValue, minValue);
          maxDoubleValue = Math.max(maxDoubleValue, maxValue);
        }
      }
    }

    private void addDoubleVector(DoubleVector vector) {

      // Promote the result from integer to double if necessary
      if(resultType == IntVector.VECTOR_TYPE) {
        resultType = DoubleVector.VECTOR_TYPE;
      }

      // No need to look at the values if we've already encountered
      // an NA value. Note that if we've encountered an NaN value,
      // we have to keep looking for an NA value.
      if(naEncountered) {
        return;
      }

      double minValue = Double.POSITIVE_INFINITY;
      double maxValue = Double.NEGATIVE_INFINITY;
      boolean hasValues = false;

      int length = vector.length();
      for (int i = 0; i < length; i++) {
        double value = vector.getElementAsDouble(i);
        if(Double.isNaN(value)) {
          if(!removeNA) {
            if (DoubleVector.isNA(value)) {
              // If we encounter an NA, we can stop processing immediately.
              naEncountered = true;
              return;
            } else {
              // If we encounter an NaN, we have to keep going to check for
              // an NA value.
              nanEncountered = true;
            }
            break;
          }
        } else {
          hasValues = true;
          if(value > maxValue) {
            maxValue = value;
          }
          if(value < minValue) {
            minValue = value;
          }
        }
      }

      if(nanEncountered) {
        if(resultType == StringVector.VECTOR_TYPE) {
          addStringRange("NaN", "NaN");
        } else {
          minDoubleValue = Double.NaN;
          maxDoubleValue = Double.NaN;
        }
      } else if(hasValues) {
        if (resultType == StringVector.VECTOR_TYPE) {
          addStringRange(NumericLiterals.format(minValue, "NA"), NumericLiterals.format(maxValue, "NA"));

        } else {
          this.minDoubleValue = Math.min(minDoubleValue, minValue);
          this.maxDoubleValue = Math.max(maxDoubleValue, maxValue);
          this.hasValues = true;
        }
      }
    }

    private void addStringVector(StringVector vector) {

      // Promote the result to string if necessary
      if(!naEncountered && hasValues) {
        if (resultType == IntVector.VECTOR_TYPE) {
          minStringValue = Integer.toString(minIntValue);
          maxStringValue = Integer.toString(maxIntValue);
        } else if(resultType == DoubleVector.VECTOR_TYPE) {
          minStringValue = NumericLiterals.toString(minDoubleValue);
          maxStringValue = NumericLiterals.toString(maxDoubleValue);
        }
      }
      resultType = StringVector.VECTOR_TYPE;

      // If we've already encountered an NA, no need to process
      // the contents of this vector.
      if(naEncountered) {
        return;
      }

      // Compute the extrema of this vector.
      String minValue = null;
      String maxValue = null;
      boolean hasValues = false;

      int length = vector.length();
      for (int i = 0; i < length; i++) {
        String value = vector.getElementAsString(i);
        if (value == null) {
          if (!removeNA) {
            naEncountered = true;
            return;
          }
        } else {
          if (hasValues) {
            minValue = min(minValue, value);
            if (value.compareTo(minValue) < 0) {
              minValue = value;
            }
            if (value.compareTo(maxValue) > 0) {
              maxValue = value;
            }
          } else {
            minValue = value;
            maxValue = value;
            hasValues = true;
          }
        }
      }

      if(hasValues) {
        addStringRange(minValue, maxValue);
      }
    }

    private String min(String x, String y) {
      if(x.compareTo(y) < 0) {
        return x;
      } else {
        return y;
      }
    }

    private String max(String x, String y) {
      if(x.compareTo(y) > 0) {
        return x;
      } else {
        return y;
      }
    }

    private void addStringRange(String minValue, String maxValue) {
      if(this.hasValues) {
        minStringValue = min(minStringValue, minValue);
        maxStringValue = max(maxStringValue, maxValue);
      } else {
        minStringValue = minValue;
        maxStringValue = maxValue;
        hasValues = true;
      }
    }

    public Vector getRange() {
      if(nanEncountered) {
        return new DoubleArrayVector(Double.NaN, Double.NaN);
      } else if(naEncountered) {
        return resultType.newBuilder().addNA().addNA().build();
      } else if(!hasValues) {
        if(resultType == StringVector.VECTOR_TYPE) {
          return new StringArrayVector(StringVector.NA, StringVector.NA);
        } else {
          // Even if the arguments are all integers, we promote the result to double
          // so that we can return an infinite value.
          return new DoubleArrayVector(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        }
      } else if(resultType == IntVector.VECTOR_TYPE) {
        return new IntArrayVector(minIntValue, maxIntValue);
      } else if(resultType == DoubleVector.VECTOR_TYPE) {
        return new DoubleArrayVector(minDoubleValue, maxDoubleValue);
      } else if(resultType == StringVector.VECTOR_TYPE) {
        return new StringArrayVector(minStringValue, maxStringValue);
      } else {
        throw new UnsupportedOperationException();
      }
    }

    public Vector getMinimum() {
      return getRange().getElementAsSEXP(0);
    }

    public Vector getMaximum() {
      return getRange().getElementAsSEXP(1);
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
  public static AtomicVector prod(@ArgumentList ListVector arguments, @NamedFlag("na.rm") boolean removeNA) {

    double realProduct = realProduct(arguments, removeNA);
    Complex complexProduct = complexProduct(arguments, removeNA);
    
    if(complexProduct == null) {
      return DoubleVector.valueOf(realProduct);
    } else {
      if(complexProduct.equals(ComplexVector.NA)) {
        return ComplexVector.valueOf(complexProduct);
      } else {
        return ComplexVector.valueOf(complexProduct.multiply(realProduct));
      }
    }
  }

  private static double realProduct(ListVector arguments, boolean removeNA) {
    double product = 1;
    for (SEXP argument : arguments) {
      if (!(argument instanceof AtomicVector)) {
        throw new EvalException("invalid 'type' (%s) of argument", argument.getTypeName());
      } else if (argument instanceof StringVector) {
        throw new EvalException("invalid 'type' (character) of argument");
      } else if(!(argument instanceof ComplexVector)) {
        AtomicVector vector = (AtomicVector) argument;
        for (int i = 0; i != vector.length(); ++i) {
          if (vector.isElementNA(i)) {
            if (!removeNA) {
              return DoubleVector.NA;
            }
          } else {
            product = product * vector.getElementAsDouble(i);
          }
        }
      }
    }
    return product;
  }

  private static Complex complexProduct(ListVector arguments, boolean removeNA) {
    Complex product = null;
    for (SEXP argument : arguments) {
      if(argument instanceof ComplexVector) {
        if(product == null) {
          product = ComplexVector.complex(1, 0);
        }
        ComplexVector vector = (ComplexVector) argument;
        for (int i = 0; i != vector.length(); ++i) {
          if (vector.isElementNA(i)) {
            if (!removeNA) {
              return ComplexVector.NA;
            }
          } else {
            product = product.multiply(vector.getElementAsComplex(i));
          }
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

    if(arguments.length() == 1 && arguments.get(0) instanceof DoubleVector && !removeNA) {
      DoubleVector argument = (DoubleVector) arguments.get(0);
      if(argument.isDeferred() || argument.length() > 300) {
        return new DeferredSum((Vector) arguments.get(0), AttributeMap.EMPTY);
      }
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
        ComplexVector vector = (ComplexVector) argument;
        haveComplex = true;
        for (int i = 0; i != vector.length(); ++i) {
          if (vector.isElementNA(i)) {
            if (!removeNA) {
              return new ComplexArrayVector(ComplexVector.NA);
            }
          } else {
            Complex z = vector.getElementAsComplex(i);
            realSum += z.getReal();
            imaginarySum += z.getImaginary();
          }
        }

      } else if(argument == Null.INSTANCE) {
        // NOOP
        
      } else {
        throw new EvalException("invalid 'type' (" + argument.getTypeName() + ") of argument");
      }
    }
    if(haveComplex) {
      return new ComplexArrayVector(ComplexVector.complex(realSum, imaginarySum));

    } else if(haveDouble) {
      return new DoubleArrayVector(realSum);

    } else {
      if(realSum < Integer.MIN_VALUE || realSum > Integer.MAX_VALUE) {
        context.warn("Integer overflow - use sum(as.numeric(.))");
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
        int value = vector.getElementAsRawLogical(i);
        if(value == IntVector.NA) {
          if(!removeNA) {
            return Logical.NA;
          }
        } else if(value != 0) {
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
        int value = vector.getElementAsRawLogical(i);
        if(value == IntVector.NA) {
          if(!removeNA) {
            return Logical.NA;
          }
        } else {
          if(value == 0) {
            return Logical.FALSE;
          }
        }
      }
    }
    return Logical.TRUE;
  }


  @Deferrable
  @Internal
  public static DoubleVector mean(Vector x) {

    if(x.isDeferred() || x.length() > 100000) {
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
