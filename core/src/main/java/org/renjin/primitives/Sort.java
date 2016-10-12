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

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Sort {

  @Internal
  public static Vector sort(StringVector x, boolean decreasing) {

    if(x.getAttribute(Symbols.NAMES)!= Null.INSTANCE) {
      throw new EvalException("sorting of vectors with names not yet implemented!");
    }

    String sorted[] = x.toArray();

    if(decreasing) {
      Arrays.sort(sorted, Collections.reverseOrder());
    }else{
      Arrays.sort(sorted);
    }

    return new StringArrayVector(sorted, x.getAttributes());
  }

  @Internal
  public static Vector sort(DoubleVector x, boolean decreasing) {

    if(x.getAttribute(Symbols.NAMES)!= Null.INSTANCE) {
      throw new EvalException("sorting of vectors with names not yet implemented!");
    }

    double sorted[] = x.toDoubleArray();

    Arrays.sort(sorted);

    if(decreasing) {
      reverse(sorted);
    }

    return (Vector) DoubleArrayVector.unsafe(sorted).setAttributes(x.getAttributes());
  }
  
  private static void reverse(double[] b) {
    int left  = 0;          
    int right = b.length-1; 

    while (left < right) {
      double temp = b[left]; 
      b[left]  = b[right]; 
      b[right] = temp;

      // move the bounds toward the center
      left++;
      right--;
    }
  }

  @Internal
  public static Vector sort(IntVector x, boolean decreasing) {

    if(x.getAttribute(Symbols.NAMES)!= Null.INSTANCE) {
      throw new EvalException("sorting of vectors with names not yet implemented!");
    }

    int sorted[] = x.toIntArray();
    
    Arrays.sort(sorted);

    if(decreasing) {
      reverse(sorted);
    }

    return new IntArrayVector(sorted, x.getAttributes());
  }

  @Internal("is.unsorted")
  public static boolean isUnsorted(AtomicVector x, boolean strictly) {
    for(int i=1;i<x.length();++i) {
      int z = x.compare(i-1, i);
      if(z > 0) {
        return true;
      } else if(strictly && z == 0) {
        return true;
      }
    }
    return false;
  }
  
  @Internal("is.unsorted")
  public static LogicalVector isUnsorted(ListVector x, boolean strictly) {
    if(x.length() <= 1) {
      return LogicalVector.FALSE;
    } else {
      return LogicalVector.NA_VECTOR;
    }
  }
 
  @Internal
  public static DoubleVector qsort(DoubleVector x, LogicalVector returnIndexes) {

    if(returnIndexes.isElementTrue(0)) {
      throw new EvalException("qsort(indexes=TRUE) not yet implemented");
    }
    
    double[] values = x.toDoubleArray();
    Arrays.sort(values);
    
    DoubleVector sorted = new DoubleArrayVector(values, x.getAttributes());
    
    // drop the names attributes if present because it will not be sorted
    return (DoubleVector)sorted
            .setAttribute(Symbols.NAMES, Null.INSTANCE);  
  }
  
  @Internal
  public static DoubleVector psort(DoubleVector x, Vector indexes) {
    // stub implementation: we just do a full sort
    return qsort(x, LogicalVector.FALSE);
  }

  @Internal
  public static IntVector qsort(IntVector x, LogicalVector returnIndexes) {

    if(returnIndexes.isElementTrue(0)) {
      throw new EvalException("qsort(indexes=TRUE) not yet implemented");
    }
    
    int[] values = x.toIntArray();
    Arrays.sort(values);
    
    IntVector sorted = new IntArrayVector(values, x.getAttributes());
    
    // drop the names attributes if present because it will not be sorted
    return (IntVector)sorted
            .setAttribute(Symbols.NAMES, Null.INSTANCE);  
  }

  @Internal
  public static IntVector psort(IntVector x, Vector indexes) {
    return qsort(x, LogicalVector.FALSE);
  }


  @Internal
  public static LogicalVector qsort(LogicalVector x, boolean returnIndexes) {

    if(returnIndexes) {
      throw new EvalException("qsort(indexes=TRUE) not yet implemented");
    }
    
    int[] array = x.toIntArray();
    
    Arrays.sort(array);

    LogicalVector sorted = new LogicalArrayVector(array, x.getAttributes());
    

    // drop the names attributes if present because it will not be sorted
    return (LogicalVector)sorted
        .setAttribute(Symbols.NAMES, Null.INSTANCE);
  }
  
  @Internal
  public static LogicalVector psort(LogicalVector x, Vector indexes) {
    return qsort(x, false);
  }

  private static void reverse(int[] b) {
    int left  = 0;          
    int right = b.length-1; 

    while (left < right) {
      int temp = b[left]; 
      b[left]  = b[right]; 
      b[right] = temp;

      // move the bounds toward the center
      left++;
      right--;
    }
  }
  
  /**
   * Returns a permutation which rearranges its first argument into ascending or
   * descending order, breaking ties by further arguments.
   *
   * <p>This function is like a spreadsheet sort function.
   * Each argument is a column.
   *
   * @param columns
   * @return
   */
  @Internal
  public static Vector order(final boolean naLast, final boolean decreasing, String method, @ArgumentList final ListVector columns) {
        
    if (columns.length() == 0) {
      return Null.INSTANCE;
    }

    int numRows = columns.getElementAsSEXP(0).length();

    for (int i = 0; i != columns.length(); ++i) {
      if (columns.getElementAsSEXP(i).length() != numRows) {
        throw new EvalException("argument lengths differ");
      }
    }

    List<Integer> ordering = Lists.newArrayListWithCapacity(numRows);
    for (int i = 0; i != numRows; ++i) {
      ordering.add(i);
    }

    Collections.sort(ordering, new Comparator<Integer>() {

      @Override
      public int compare(Integer row1, Integer row2) {
        int col = 0;
        int rel;
        while ((rel = compare(row1, row2, col)) == 0) {
          col++;
          if (col == columns.length()) {
            return 0;
          }
        }
        return rel;
      }

      private int compare(Integer row1, Integer row2, int col) {
        AtomicVector column = (AtomicVector) columns.get(col);
        boolean na1 = column.isElementNA(row1);
        boolean na2 = column.isElementNA(row2);
        if(na1 && na2) {
          // Both values are NA, consider equal
          return 0;
        } else if(na1) {
          // NA <-> 42
          return naLast ? +1 : -1;
        } else if(na2) {
          // 42 <-> NA
          return naLast ? -1 : +1;
        } else {
          // 42 <-> 41
          return decreasing ?
              -column.compare(row1, row2) :
              +column.compare(row1, row2);
        }
      }

    });

    IntArrayVector.Builder result = new IntArrayVector.Builder();
    for (Integer index : ordering) {
      result.add(index + 1);
    }

    return result.build();
  }   

  @Internal("which.min")
  public static IntVector whichMin(Vector input) {
    int minIndex = -1;
    double minValue = 0;

    for (int i = 0; i < input.length(); i++) {
      double value = input.getElementAsDouble(i);
      if (!Double.isNaN(value)) {
        if(minIndex == -1 || value < minValue) {
          minValue = input.getElementAsDouble(i);
          minIndex = i;          
        }
      }
    }

    if(minIndex >= 0) {
      return new IntArrayVector(new int[] { minIndex + 1 }, whichName(input, minIndex));
    } else {
      return IntVector.EMPTY;
    }
  }

  @Internal("which.max")
  public static IntVector whichMax(Vector input) {
    int maxIndex = -1;
    double maxValue = 0;

    for (int i = 0; i < input.length(); i++) {
      double value = input.getElementAsDouble(i);
      if (!Double.isNaN(value)) {
        if(maxIndex == -1 || value > maxValue) {
          maxValue = input.getElementAsDouble(i);
          maxIndex = i;
        }
      }
    }

    if(maxIndex >= 0) {
      return new IntArrayVector(new int[] { maxIndex + 1 }, whichName(input, maxIndex));
    } else {
      return IntVector.EMPTY;
    }
  }

  private static AttributeMap whichName(Vector v, int index) {
    AttributeMap attributes;
    AtomicVector names = v.getNames();
    if(names != Null.INSTANCE) {
      String maxName = names.getElementAsString(index);
      attributes = AttributeMap.newBuilder().setNames(new StringArrayVector(maxName)).build();
    } else {
      attributes = AttributeMap.EMPTY;
    }
    return attributes;
  }

  @Internal
  public static Vector rank(final AtomicVector input, String tiesMethod) {

    boolean decreasing = false;

    AtomicVector sortedInput;

    String typeVector = input.getTypeName();
    switch (typeVector){
      case "character":
        StringVector inputStringVector = ((StringVector) input.setAttributes(AttributeMap.EMPTY));
        sortedInput = ((AtomicVector) sort(inputStringVector, decreasing));
        break;
      case "double":
        DoubleVector inputDoubleVector = ((DoubleVector) input.setAttributes(AttributeMap.EMPTY));
        sortedInput = ((AtomicVector) sort(inputDoubleVector, decreasing));
        break;
      default:
        IntVector inputIntVector = ((IntVector) input.setAttributes(AttributeMap.EMPTY));
        sortedInput = ((AtomicVector) sort(inputIntVector, decreasing));
        break;
    }




    switch(tiesMethod.toUpperCase()){
      case "MIN":
        return rankMin(input, sortedInput);

      case "MAX":
        return rankMax(input, sortedInput);

      case "AVERAGE":
        return rankAverage(input, sortedInput);

      case "FIRST":
        throw new EvalException("ties.method=first not implemented");


      case "RANDOM":
        throw new EvalException("ties.method=random not implemented");


      default:
        throw new EvalException("Invalid ties.method.");

    }

  }

  private static Vector rankAverage(AtomicVector input, AtomicVector sortedInput) {
    DoubleArrayVector.Builder ranks = new DoubleArrayVector.Builder();
    for ( int i=0; i < sortedInput.length(); i++ ) {
      int minRank = sortedInput.indexOf(input, i, 0);
      int maxRank = minRank;
      while ( maxRank+1 < sortedInput.length() &&
              sortedInput.compare(minRank, maxRank+1) == 0) {
        maxRank++;
      }

      double average = (((double) minRank) + ((double) maxRank)) / 2d;
      ranks.add(average + 1);
    }
    return ranks.build();
  }

  private static Vector rankMax(AtomicVector input, AtomicVector sortedInput) {
    IntArrayVector.Builder ranks = new IntArrayVector.Builder();
    for ( int i=0; i < sortedInput.length(); i++ ) {
      int minRank = sortedInput.indexOf(input, i, 0);
      int maxRank = minRank;
      while ( maxRank+1 < sortedInput.length() &&
              sortedInput.compare(minRank, maxRank+1) == 0) {
        maxRank++;
      }
      ranks.add(maxRank + 1);
    }
    return ranks.build();
  }

  private static Vector rankMin(AtomicVector input, AtomicVector sortedInput) {
    IntArrayVector.Builder ranks = new IntArrayVector.Builder();

    for (int i=0; i < sortedInput.length(); i++) {
      ranks.add( sortedInput.indexOf( input, i, 0 ) + 1 );
    }
    return ranks.build();
  }

  @Builtin
  @Generic
  public static SEXP xtfrm(@Current Context context, SEXP x) {
    FunctionCall defaultCall = FunctionCall.newCall(Symbol.get("xtfrm.default"), x);
    return context.evaluate(defaultCall);
  }
}
