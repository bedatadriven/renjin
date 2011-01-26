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

package r.lang;

public class IndexUtils {

  public static int arrayIndexToVectorIndex(int arrayIndex[], int dim[]) {
    int vectorIndex = 0;
    int offset = 1;
    for(int i=0;i!=dim.length;++i) {
      vectorIndex += arrayIndex[i] * offset;
      offset *= dim[i];
    }
    return vectorIndex;
  }

  public static void vectorIndexToArrayIndex(int vectorIndex, int arrayIndex[], int dim[]) {
    for(int i=0;i!=dim.length;++i) {
      arrayIndex[i] = vectorIndex % dim[i];
      vectorIndex = (vectorIndex - arrayIndex[i]) / dim[i];
    }
  }

  public static int[] vectorIndexToArrayIndex(int vectorIndex, int dim[]) {
    int index[] = new int[dim.length];
    vectorIndexToArrayIndex(vectorIndex, index, dim);
    return index;
  }

  /**
   * Increments an array index by one
   * @param arrayIndex the array index
   * @param dim an array containing the lengths of each dimension of the array
   * @return  true if the
   */
  public static boolean incrementArrayIndex(int arrayIndex[], int dim[]) {
    for(int i=0;i!=arrayIndex.length;++i) {
      if(arrayIndex[i]+1 < dim[i]) {
        arrayIndex[i] = arrayIndex[i]+1;
        for(int j=0;j!=i;++j) {
          arrayIndex[j] = 0;
        }
        return true;
      }
    }
    return false;
  }
}
