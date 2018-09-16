/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.renjin.invoke.annotations.Internal;
import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.sexp.*;

import java.lang.System;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

public class Merge {


  private interface RowPredicate {
    boolean equal(int leftRowIndex, int rightRowIndex);
  }

  private interface RowHashFunction {
    int hashCode(int rowIndex);
  }

  @Internal
  public static SEXP merge(ListVector left, ListVector right, IntVector byLeft, IntVector byRight,
                           boolean allLeft, boolean allRight, boolean sort) {

    int nleft = left.getElementAsSEXP(0).length();
    int nright = right.getElementAsSEXP(0).length();
    int nkeys = byLeft.length();


    System.out.printf("merge: nleft = %d, right = %d (%s), keys = %d, all.x = %s, all.y = %s, %n", nleft, nright,
        Integer.toHexString(System.identityHashCode(right)),
        nkeys, allLeft, allRight);


    if(!sort) {
      if (nright > nleft) {
        return hashAndMerge(left, byLeft, right, byRight, allLeft, allRight);
      } else {
        return hashAndMerge(right, byRight, left, byLeft, allRight, allLeft);
      }
    }

    return Null.INSTANCE;
  }

  /**
   *
   * @param dataFrame a data frame list
   * @param keyNumbers integers indicating the position of the key in the table. key number 0 refers to the
   *                   table's row names, all others are 1-based column indices
   * @return an array of {@code AtomicVectors} containing the selected keys
   */
  private static AtomicVector[] keyArray(ListVector dataFrame, IntVector keyNumbers) {
    int nkeys = keyNumbers.length();
    AtomicVector keys[] = new AtomicVector[nkeys];
    for (int i = 0; i < nkeys; i++) {
      int keyNumber = keyNumbers.getElementAsInt(i);
      if(keyNumber == 0) {
        keys[i] = (AtomicVector) dataFrame.getAttribute(Symbols.ROW_NAMES);
      } else {
        keys[i] = (AtomicVector) dataFrame.getElementAsSEXP(keyNumber - 1);
      }
    }
    return keys;
  }

  /**
   * Merges the two data.frames by first constructing a hash table of the smaller data.frame and then
   * scanning the larger data.frame.
   *
   * @param left the left (and smaller) table
   * @param byLeft the key numbers of the left table
   * @param right the right (and larger) table
   * @param byRight the key numbers of the right table
   * @param allLeft true if all rows from the left-handed data.frame should be included, even those not matching
   *                any rows in the right-handed table.
   * @param allRight true if all rows from the right-handed data.frame should be included, even those not matching
   *                 any rows in the left-handed table.
   * @return the merged data frames.
   */
  private static ListVector hashAndMerge(
      ListVector left, IntVector byLeft,
      ListVector right, IntVector byRight,
      boolean allLeft,
      boolean allRight) {

    AtomicVector[] leftKeys = keyArray(left, byLeft);
    AtomicVector[] rightKeys = keyArray(right, byRight);

    int nLeftRows = leftKeys[0].length();
    int nRightRows = rightKeys[0].length();

    assert nLeftRows < nRightRows;

    /* Now scan the larger table, looking for matches in the smaller table */

    IntArrayVector.Builder leftRows = new IntArrayVector.Builder();
    IntArrayVector.Builder rightRows = new IntArrayVector.Builder();
    IntArrayVector.Builder rightOnlyRows = new IntArrayVector.Builder();

    if(allRight) {
      rightOnlyRows = new IntArrayVector.Builder();
    }

    int[][] leftHashTable = hashRows(leftKeys);
    int leftTableSize = leftHashTable.length;

    RowHashFunction rightHashFunction = hashFunction(rightKeys);
    RowPredicate rowPredicate = rowPredicate(leftKeys, rightKeys);


    BitSet leftMatched = null;
    if(allLeft) {
      leftMatched = new BitSet(nLeftRows);
    }

    for (int rightRowIndex = 0; rightRowIndex < nRightRows; rightRowIndex++) {
      int rowHash = rightHashFunction.hashCode(rightRowIndex);
      int bucketIndex = bucketIndex(leftTableSize, rowHash);
      int[] bucket = leftHashTable[bucketIndex];
      boolean matched = false;
      if(bucket != null) {
        for(int slotIndex = 0; slotIndex < bucket.length; ++slotIndex) {
          int leftRowIndex = bucket[slotIndex];
          if(leftRowIndex == -1) {
            break;
          }
          if(rowPredicate.equal(leftRowIndex, rightRowIndex)) {
            matched = true;
            rightRows.add(rightRowIndex);
            leftRows.add(leftRowIndex);
            if (leftMatched != null) {
              leftMatched.set(leftRowIndex);
            }
          }
        }
      }
      if(allRight && !matched) {
        rightOnlyRows.add(right);
      }
    }

    if(leftMatched != null) {
      int leftRowIndex = leftMatched.nextClearBit(0);
      while(leftRowIndex < nLeftRows) {
        leftRows.add(leftRowIndex);
        rightRows.add(IntVector.NA);
        leftRowIndex = leftMatched.nextClearBit(leftRowIndex + 1);
      }
    }

    return merge(left, byLeft, leftRows.build(), right, byRight, rightRows.build(), rightOnlyRows.build());
  }

  /**
   * @param left the left-handed table
   * @param leftKeyNumbers the key numbers of the left-handed table
   * @param leftRows the 0-based indices of the selected rows from the left hand table,
   * @param right the right-handed table
   * @param rightKeyNumbers the key numbers of the right-handed table
   * @param rightRows the 0-based indices of the selected rows from the right handed table, with NAs for rows that
   *                  correspond to unmatched left-handed rows that should be left blank
   * @param rightOnlyRows 0-based indices of rows from the right-handed table that do not match any rows
   *                      in the left-handed table.
   *
   * @return the merged data.frame
   */
  private static ListVector merge(
      ListVector left, IntVector leftKeyNumbers, IntVector leftRows,
      ListVector right, IntVector rightKeyNumbers, IntVector rightRows, IntVector rightOnlyRows) {

    ListVector.NamedBuilder list = new ListVector.NamedBuilder();

    StringVector leftNames = (StringVector) left.getAttribute(Symbols.NAMES);
    StringVector rightNames = (StringVector) right.getAttribute(Symbols.NAMES);

    // Add keys first
    for (int keyIndex = 0; keyIndex < leftKeyNumbers.length(); keyIndex++) {
      int keyNumber = leftKeyNumbers.getElementAsInt(keyIndex);
      if(keyNumber == 0) {
        throw new UnsupportedOperationException("TODO");
      } else {
        String name = leftNames.getElementAsString(keyNumber - 1);
        Vector column = selectKey((Vector) left.getElementAsSEXP(keyNumber - 1), leftRows,
                                  (Vector) right.getElementAsSEXP(keyNumber - 1), rightOnlyRows );
        list.add(name, column);
      }
    }

    // Add other columns from the left first
    for (int i = 0; i < left.length(); i++) {
      if(!isKey(i, leftKeyNumbers)) {
        String name = leftNames.getElementAsString(i);
        Vector column = selectLeft((Vector)left.getElementAsSEXP(i), leftRows, rightOnlyRows);
        list.add(name, column);
      }
    }

    // Add other columns from the right
    for (int i = 0; i < right.length(); i++) {
      if(!isKey(i, rightKeyNumbers)) {
        String name = rightNames.getElementAsString(i);
        Vector column = selectRight((Vector)right.getElementAsSEXP(i), rightRows, rightOnlyRows);
        list.add(name, column);
      }
    }

    list.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(leftRows.length()));
    list.setAttribute(Symbols.CLASS, StringVector.valueOf("data.frame"));

    return list.build();
  }


  /**
   *
   * @param columnIndex 0-based column index
   * @return true if the given column index is included as a merge key
   */
  private static boolean isKey(int columnIndex, IntVector keyNumbers) {
    for (int i = 0; i < keyNumbers.length(); i++) {
      if(columnIndex + 1 == keyNumbers.getElementAsInt(i)) {
        return true;
      }
    }
    return false;
  }


  /**
   * Constructs a result vector for a key column
   */
  private static Vector selectKey(Vector leftKey, IntVector leftRows, Vector rightKey, IntVector rightOnlyRows) {
    Vector.Builder builder = leftKey.newBuilderWithInitialCapacity(leftKey.length() + rightOnlyRows.length());

    addRows(builder, leftKey, leftRows);
    addRows(builder, rightKey, rightOnlyRows);

    return builder.build();
  }

  private static Vector selectLeft(Vector leftColumn, IntVector leftRows, IntVector rightOnlyRows) {
    Vector.Builder builder = leftColumn.newBuilderWithInitialCapacity(leftRows.length() + rightOnlyRows.length());

    addRows(builder, leftColumn, leftRows);
    builder.addNA(rightOnlyRows.length());

    return builder.build();
  }

  private static Vector selectRight(Vector rightColumn, IntVector rightRows, IntVector rightOnlyRows) {

    Vector.Builder builder = rightColumn.newBuilderWithInitialCapacity(rightRows.length() + rightOnlyRows.length());

    addRows(builder, rightColumn, rightRows);
    addRows(builder, rightColumn, rightOnlyRows);

    return builder.build();
  }


  private static void addRows(Vector.Builder builder, Vector column, IntVector rowIndices) {
    for (int i = 0; i < rowIndices.length(); i++) {
      int rowIndex = rowIndices.getElementAsInt(i);
      if(IntVector.isNA(rowIndex)) {
        builder.addNA();
      } else {
        builder.addFrom(column, rowIndex);
      }
    }
  }


  /**
   * Build a hash table of the smaller data.frame.
   *
   */
  private static int[][] hashRows(AtomicVector[] keys) {
    int nrows = keys[0].length();
    int nbuckets = 256;

    int table[][] = new int[nbuckets][];

    RowHashFunction rowHashFunction = hashFunction(keys);

    for (int rowIndex = 0; rowIndex < nrows; rowIndex++) {
      int rowHash = rowHashFunction.hashCode(rowIndex);
      int bucketIndex = bucketIndex(nbuckets, rowHash);

      int[] bucket = table[bucketIndex];
      if(bucket == null) {
        bucket = new int[10];
        Arrays.fill(bucket, -1);

        table[bucketIndex] = bucket;

      } else {
        // Check to see if we need to resize the array
        int bucketSize = bucket.length;
        if(bucket[bucketSize - 1] != -1) {
          bucket = Arrays.copyOf(bucket, Math.min(bucketSize * 2, nrows));
          Arrays.fill(bucket, bucketSize, bucket.length, -1);
          table[bucketIndex] = bucket;
        }
      }
      int slotIndex = 0;
      while(bucket[slotIndex] != -1) {
        slotIndex++;
        if(slotIndex == bucket.length) {
          throw new UnsupportedOperationException("TODO");
        }
      }
      bucket[slotIndex] = rowIndex;
    }
    return table;
  }

  /**
   * Compute a bucket index from the row hash and the number of buckets in our hash table.
   */
  private static int bucketIndex(int nbuckets, int rowHash) {
    int spreadHashCode = rowHash ^ (rowHash >>> 16);
    return (nbuckets - 1) & spreadHashCode;
  }

  /**
   * Construct a hash function from an array of key vectors.
   */
  private static RowHashFunction hashFunction(AtomicVector[] keyColumns) {
    if(keyColumns.length == 1) {
      AtomicVector key = keyColumns[0];
      return key::elementHash;

    } else if(keyColumns.length == 2) {
      AtomicVector key1 = keyColumns[0];
      AtomicVector key2 = keyColumns[1];
      return (rowIndex -> key1.elementHash(rowIndex) * 31 + key2.elementHash(rowIndex));

    } else if(keyColumns.length == 3) {
      AtomicVector key1 = keyColumns[0];
      AtomicVector key2 = keyColumns[1];
      AtomicVector key3 = keyColumns[2];
      return (rowIndex -> key1.elementHash(rowIndex) * 31 +
                          key2.elementHash(rowIndex) * 31 +
                          key3.elementHash(rowIndex));
    } else {
      throw new UnsupportedOperationException("TODO");
    }
  }

  /**
   * Construct a function which tests equality between the keys of two rows.
   */
  private static RowPredicate rowPredicate(AtomicVector[] leftKeys, AtomicVector[] rightKeys) {
    int nkeys = leftKeys.length;
    RowPredicate[] keyPredicates = new RowPredicate[nkeys];
    for (int i = 0; i < nkeys; i++) {
      AtomicVector leftKey = leftKeys[i];
      AtomicVector rightKey = rightKeys[i];
      keyPredicates[i] = rowPredicate(leftKey, rightKey);
    }

    if(nkeys == 1) {
      return keyPredicates[0];
    } else if(nkeys == 2) {
      return (i, j) -> keyPredicates[0].equal(i, j) &&
                       keyPredicates[1].equal(i, j);
    } else if(nkeys == 3) {
      return (i, j) -> keyPredicates[0].equal(i, j) &&
                       keyPredicates[1].equal(i, j) &&
                       keyPredicates[2].equal(i, j);
    } else {
      throw new UnsupportedOperationException("TODO");
    }
  }

  private static RowPredicate rowPredicate(AtomicVector leftKey, AtomicVector rightKey) {
    if(leftKey instanceof StringVector) {
      return (i, j) -> Objects.equals(leftKey.getElementAsString(i), rightKey.getElementAsString(j));
    } else if(leftKey instanceof IntVector) {
      return (i, j) -> leftKey.getElementAsInt(i) == rightKey.getElementAsInt(j);
    } else if(leftKey instanceof DoubleVector) {
      return (i, j) -> leftKey.getElementAsDouble(i) == rightKey.getElementAsDouble(j);
    } else {
      throw new UnsupportedOperationException("TODO");
    }
  }

}
