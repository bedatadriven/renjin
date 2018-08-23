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
package org.renjin.primitives.vector;

import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

public class RowNamesVector extends StringVector {

  private final int length;
  
  public RowNamesVector(int length, AttributeMap attributes) {
    super(attributes);
    this.length = length;
  }

  public RowNamesVector(int numRows) {
    this(numRows, AttributeMap.EMPTY);
  }

  @Override
  public String getElementAsString(int index) {
    return Integer.toString(index + 1);
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new RowNamesVector(length, attributes);
  }
  
  /**
   * GNU R uses a peculiar scheme to compress row names in the form of 
   * ["1","2","3"].. etc. Rather than storing the whole string vector in this
   * form, an integer vector is stored in its place with the convention [NA, -numRows].
   * 
   * <p>This is no longer needed in Renjin since we don't actually have to materialize
   * the contents of the array, but it does appear in the R code from time to time so
   * we have to handle it appropriately.
   * 
   * @param rowNames row names vector
   * @return true if it is the old compact format
   */
  public static boolean isOldCompactForm(Vector rowNames) {
    return rowNames.length() == 2 && rowNames.isElementNA(0) && rowNames.getElementAsInt(1) < 0;
  }

  public static boolean isCompactForm(Vector rowNames) {
    return rowNames.length() == 2 && rowNames.isElementNA(0) && rowNames.getElementAsInt(1) > 0;
  }
  
  public static RowNamesVector fromOldCompactForm(SEXP rowNames) {
    Preconditions.checkArgument(isOldCompactForm(rowNames));
    int numRows = -((Vector)rowNames).getElementAsInt(1);
    return new RowNamesVector(numRows, AttributeMap.EMPTY);
  }

  public static boolean isOldCompactForm(SEXP rowNames) {
    if(rowNames instanceof Vector) {
      return isOldCompactForm((Vector)rowNames);
    } else {
      return false;
    }
  }

  public static boolean isCompactForm(SEXP rowNames) {
    if(rowNames instanceof Vector) {
      return isCompactForm((Vector)rowNames);
    } else {
      return false;
    }
  }

  public static Vector purify(SEXP sexp) {
    if(isOldCompactForm(sexp)) {
      return fromOldCompactForm(sexp);
    } else {
      return (Vector)sexp;
    }
  }
}