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

import com.google.common.collect.Lists;
import r.jvmi.annotations.ArgumentList;
import r.lang.*;
import r.lang.exception.EvalException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Sort {

    public static Vector sort(StringVector x, boolean decreasing) {

    if(x.getAttribute(Symbol.NAMES)!= Null.INSTANCE) {
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


    /**
     * Returns a permutation which rearranges its first argument into ascending or
     * descending order, breaking ties by further arguments.
     *
     * <p>This function is like a spreadsheet sort function.
     * Each argument is a column.
     *
     * @param arguments
     * @return
     */
    public static Vector order(@ArgumentList ListVector arguments) {
        if (arguments.length() < 2) {
            throw new EvalException("expected at least two arguments");
        }
        if (arguments.length() == 2) {
            return Null.INSTANCE;
        }
        final List<AtomicVector> columns = Lists.newArrayList();
        int numRows = arguments.getElementAsSEXP(2).length();
        int numColumns = arguments.length() - 2;

        for (int i = 2; i != arguments.length(); ++i) {
            if (arguments.getElementAsSEXP(i).length() != numRows) {
                throw new EvalException("argument lengths differ");
            }
            columns.add((AtomicVector) arguments.getElementAsSEXP(i));
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
                    if (col == columns.size()) {
                        return 0;
                    }
                }
                return rel;
            }

            private int compare(Integer row1, Integer row2, int col) {
                return columns.get(col).compare(row1, row2);
            }
        });

        IntVector.Builder result = new IntVector.Builder();
        for (Integer index : ordering) {
            result.add(index + 1);
        }

        return result.build();
    }
}
