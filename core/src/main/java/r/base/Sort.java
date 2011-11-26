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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import r.jvmi.annotations.ArgumentList;
import r.jvmi.annotations.NamedFlag;
import r.jvmi.annotations.Primitive;
import r.lang.AtomicVector;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.LogicalVector;
import r.lang.Null;
import r.lang.StringVector;
import r.lang.Symbols;
import r.lang.Vector;
import r.lang.exception.EvalException;

import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;

public class Sort {

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

    return new StringVector(sorted, x.getAttributes());
  }

  public static Vector sort(DoubleVector x, boolean decreasing) {

    if(x.getAttribute(Symbols.NAMES)!= Null.INSTANCE) {
      throw new EvalException("sorting of vectors with names not yet implemented!");
    }

    double sorted[] = x.toDoubleArray();

    Arrays.sort(sorted);

    if(decreasing) {
      reverse(sorted);
    }

    return new DoubleVector(sorted, x.getAttributes());
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

  public static Vector sort(IntVector x, boolean decreasing) {

    if(x.getAttribute(Symbols.NAMES)!= Null.INSTANCE) {
      throw new EvalException("sorting of vectors with names not yet implemented!");
    }

    int sorted[] = x.toIntArray();
    
    Arrays.sort(sorted);

    if(decreasing) {
      reverse(sorted);
    }

    return new IntVector(sorted, x.getAttributes());
  }

  public static DoubleVector qsort(DoubleVector x, LogicalVector returnIndexes) {

    if(returnIndexes.isElementTrue(0)) {
      throw new EvalException("qsort(indexes=TRUE) not yet implemented");
    }
    
    double[] values = x.toDoubleArray();
    Arrays.sort(values);
    
    DoubleVector sorted = new DoubleVector(values, x.getAttributes());
    
    // drop the names attributes if present because it will not be sorted
    return (DoubleVector)sorted
            .setAttribute(Symbols.NAMES, Null.INSTANCE);  
  }

  public static IntVector qsort(IntVector x, LogicalVector returnIndexes) {

    if(returnIndexes.isElementTrue(0)) {
      throw new EvalException("qsort(indexes=TRUE) not yet implemented");
    }
    
    int[] values = x.toIntArray();
    Arrays.sort(values);
    
    IntVector sorted = new IntVector(values, x.getAttributes());
    
    // drop the names attributes if present because it will not be sorted
    return (IntVector)sorted
            .setAttribute(Symbols.NAMES, Null.INSTANCE);  
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
  public static Vector order(boolean naLast, final boolean decreasing, @ArgumentList final ListVector columns) {
        
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
        return decreasing ? -rel : rel;
      }

      private int compare(Integer row1, Integer row2, int col) {
        return ((AtomicVector)columns.get(col)).compare(row1, row2);
      }
    });

    IntVector.Builder result = new IntVector.Builder();
    for (Integer index : ordering) {
      result.add(index + 1);
    }

    return result.build();
  }   

  @Primitive("which.min")
  public static IntVector whichMin(Vector v) {
    if (v.length() == 0) {
      IntVector.Builder b = new IntVector.Builder();
      return (b.build());
    }
    int minIndex = 0;
    double globalMin = v.getElementAsDouble(0);
    //this loop would be started from 1 but it needs more code. I think this is fine.
    for (int i = 0; i < v.length(); i++) {
      if (v.getElementAsDouble(i) < globalMin) {
        globalMin = v.getElementAsDouble(i);
        minIndex = i;
      }
    }
    return (new IntVector(minIndex + 1));
  }

  @Primitive("which.max")
  public static IntVector whichMax(Vector v) {
    if (v.length() == 0) {
      IntVector.Builder b = new IntVector.Builder();
      return (b.build());
    }
    int maxIndex = 0;
    double globalMax = v.getElementAsDouble(0);
    for (int i = 0; i < v.length(); i++) {
      if (v.getElementAsDouble(i) > globalMax) {
        globalMax = v.getElementAsDouble(i);
        maxIndex = i;
      }
    }
    return (new IntVector(maxIndex + 1));
  }
}
