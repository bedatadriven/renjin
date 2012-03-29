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

/**
 * Utility class for mapping matrix and array indices to vector indices. 
 * 
 * <p>All R values, whether "dimensionless" vectors, matrices, or higher order
 * arrays, are stored internally as a single dimensioned array block. These methods
 * help translate matrix and array indices to the storage index and back again.
 */
public class Indexes {

  /**
   * Translates an array index of arbitrary dimensionality to a storage vector index.
   *  
   * @param arrayIndex zero-based array index, such as {@code new int[] {0,1,3} }
   * @param dim the dimension lengths, in the same format as the R-language {@code dim} attribute.
   * @return a zero-based vector index that can be passed to {@code Vector.getElementAsXXX()} calls.
   */
  public static int arrayIndexToVectorIndex(int arrayIndex[], int dim[]) {
    int vectorIndex = 0;
    int offset = 1;
    for(int i=0;i!=dim.length;++i) {
      vectorIndex += arrayIndex[i] * offset;
      offset *= dim[i];
    }
    return vectorIndex;
  }

  /**
   * Translates a matrix (row, column) index to a storage vector index.
   * 
   * @param row zero-based row index
   * @param col zero-based column index
   * @param nrows the number of rows in the matrix
   * @param ncols the number of columns in the matrix
   * @return a zero-based vector index that can be passed to {@code Vector.getElementAsXXX()} calls.
   */
  public static int matrixIndexToVectorIndex(int row, int col, int nrows, int ncols) {
    return row + (col * nrows);
  }
  
  /**
   * Translates a vector index to a matrix row in a matrix with {@code nrows}
   * @param vectorIndex zero-based storage vector index
   * @param nrows number of rows in the matrix
   * @return the corresponding matrix row
   */
  public static int vectorIndexToRow(int vectorIndex, int nrows) {
    return vectorIndex % nrows;
  }


  /**
   * Translates a vector index to a matrix column in a matrix with {@code nrows}
   * @param vectorIndex zero-based storage vector index
   * @param nrows number of rows in the matrix
   * @return the corresponding matrix row
   */
  public static Integer vectorIndexToCol(int vectorIndex, int numRows, int numCols) {
    int row = vectorIndex % numRows;
    vectorIndex = (vectorIndex - row) / numRows;
    return vectorIndex % numCols;
  }
  
  /**
   * Translates a storage vector index to an array index within an array of arbitrary dimensionality.
   * 
   * @param vectorIndex zero-based vector index
   * @param arrayIndex an array which is updated with the results
   * @param dim the lengths of the array's dimensions, in the same format as the R-language {@code dim} attribute.
   */
  public static void vectorIndexToArrayIndex(int vectorIndex, int arrayIndex[], int dim[]) {
    for(int i=0;i!=dim.length;++i) {
      arrayIndex[i] = vectorIndex % dim[i];
      vectorIndex = (vectorIndex - arrayIndex[i]) / dim[i];
    }
  }

  /**
   * Translates a storage vector index to an array index within an array of arbitrary dimensionality.
   * @param vectorIndex zero-based vector index
   * @param dim the lengths of the array's dimensions, in the same format as the R-language {@code dim} attribute.
   * @return the array index
   */
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
