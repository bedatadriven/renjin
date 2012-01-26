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

import java.util.List;

import r.base.variance.VarianceCalculator;
import r.jvmi.annotations.ArgumentList;
import r.jvmi.annotations.NamedFlag;
import r.jvmi.annotations.Primitive;
import r.lang.AtomicVector;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.Logical;
import r.lang.LogicalVector;
import r.lang.Null;
import r.lang.SEXP;
import r.lang.Symbols;
import r.lang.Vector;
import r.lang.exception.EvalException;

/**
 * Summary group functions of vectors such as min, max, sum, etc.
 */
public class Summary {

  private Summary() {}

  public static SEXP min(@ArgumentList ListVector arguments,
                         @NamedFlag("na.rm") boolean removeNA) {

    return range(arguments, removeNA).getElementAsSEXP(0);
  }

  public static SEXP max(@ArgumentList ListVector arguments,
                         @NamedFlag("na.rm") boolean removeNA) {

    return range(arguments, removeNA).getElementAsSEXP(1);
  }


  /**
   * range returns a vector containing the minimum and maximum of all the given arguments.
   * (And recurses through lists!)
   *
   * @param arguments  any numeric or character objects.
   * @param removeNA indicating if NA's should be omitted.
   * @return a vector containing the minimum and maximum of all the given arguments.
   */
  public static Vector range(@ArgumentList ListVector arguments,
                           @NamedFlag("na.rm") boolean removeNA) {

    Range range = new Range();
    range.setRemoveNA(removeNA);
    try {
      range.addList(arguments);
    } catch (RangeContainsNA containsNA) {
      return containsNA.result;
    }
    return range.result();
  }
  
  private static class RangeContainsNA extends Exception {
    private Vector result;

    public RangeContainsNA(Vector result) {
      super();
      this.result = result;
    }
    
  }
  
  private static class Range {
    boolean removeNA;
    Vector minValue = null;
    Vector maxValue = null;
    Vector.Type resultType = IntVector.VECTOR_TYPE;

    public void setRemoveNA(boolean removeNA) {
      this.removeNA = removeNA;
    }
    
    public void addList(ListVector list) throws RangeContainsNA {
      for(SEXP argument : list) {
        if(argument instanceof ListVector) {
          addList((ListVector)argument);
        } else if(argument instanceof AtomicVector) {
          addVector(argument);
        } else {
          throw new EvalException("range() contains illegal element type '" + argument.getTypeName() + "'");
        }
      }
    }

    private void addVector(SEXP argument) throws RangeContainsNA {
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
            throw new RangeContainsNA(result.build());
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
    
    public Vector result() {
      if(maxValue == null) {
        return new DoubleVector(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
      } else {
        Vector.Builder result = resultType.newBuilder();
        result.addFrom(minValue, 0);
        result.addFrom(maxValue, 0);
        return result.build();
      }
    }
  }

  /**
   *  returns the product of all the values present in its arguments.
   *
   * @param arguments
   * @param removeNA
   * @return
   */
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
  
  
  @Primitive("mean")
  public static Vector mean(Vector x) {
    double mean = 0.0;
    for (int i=0;i<x.length();i++){
      mean+=x.getElementAsSEXP(i).asReal();
    }
    return(new DoubleVector(new double[]{mean / x.length()}));
  }
  
  
  /* co[vr](x, y, use =
    { 1,        2,      3,         4,       5  }
  "all.obs", "complete.obs", "pairwise.complete", "everything", "na.or.complete"
      kendall = TRUE/FALSE)
  */
  @Primitive("cov")
  public static Vector cov(Vector x, Vector y, int naMethod, boolean useKendall){
    if(y == null) {
      throw new UnsupportedOperationException("Covariance with x matrix only is not implemented yet");
    }
    
    if(useKendall){
      throw new UnsupportedOperationException("Kendall method is not implemented yet");
    }
    
    if(x.getAttribute(Symbols.DIM).asReal() > 1 && y.getAttribute(Symbols.DIM).asReal()>1){
      throw new UnsupportedOperationException("Covariance with matrices is not implemented yet");
    }
       
    /*
     * That means, covariance is now variance
     */
    if(y.length() == 0) y = x;
    
    double meanx=mean(x).asReal();
    double meany=mean(y).asReal();
    double sum2 = 0.0;
    for (int i=0;i<x.length();i++){
      sum2 += (x.getElementAsDouble(i) -meanx) * (y.getElementAsDouble(i) -meany);
    }
    return(new DoubleVector(new double[]{sum2 /(x.length()-1)}));
  }
  
  
  @Primitive("cumsum")
  public static DoubleVector cumsum(Vector source) {
    DoubleVector.Builder result = new DoubleVector.Builder();
    double sum = source.getElementAsDouble(0);
    result.add(sum);
    for (int i = 1; i < source.length(); i++) {
      sum += source.getElementAsDouble(i);
      if (Double.isNaN(sum)) {
        result.addNA();
      } else {
        result.add(sum);
      }
    }
    return (result.build());
  }

  @Primitive("cumprod")
  public static DoubleVector cumprod(Vector source) {
    DoubleVector.Builder result = new DoubleVector.Builder();
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

  @Primitive("cummax")
  public static DoubleVector cummax(Vector source) {
    DoubleVector.Builder result = new DoubleVector.Builder();
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

  @Primitive("cummin")
  public static DoubleVector cummin(Vector source) {
    DoubleVector.Builder result = new DoubleVector.Builder();
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

  @Primitive("pmin")
  public static Vector pmin(boolean naRm, @ArgumentList ListVector vectors) {
    ParallelProcessor processor = new ParallelProcessor(naRm, vectors) {
      @Override
      boolean predicate(Number x, Number y) {
        return ((Comparable)x).compareTo(y) > 0;
      }
    };
    return processor.compute();
  }
  
  @Primitive("pmax")
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
        return new DoubleVector.Builder();
      } else {
        return new IntVector.Builder();
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

  public static DoubleVector cor(AtomicVector x, AtomicVector y, int naMethod, boolean kendall) {
    
    if(kendall) {
      throw new EvalException("kendall=true nyi");
    }

    VarianceCalculator calculator = new VarianceCalculator(x, y, naMethod);
    return calculator.calculate();
    
  }
 }
