/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

public enum Logical {
  TRUE(1),
  FALSE(0),
  NA(IntExp.NA);

  Logical(int internalValue) {
    this.internalValue = internalValue;
  }

  private int internalValue;

  public static Logical valueOf(boolean b) {
    return b ? TRUE : FALSE;
  }

  public static Logical valueOf(int i) {
    if(i == 1) {
      return TRUE;
    } else if(i == 0) {
      return FALSE;
    } else {
      return NA;
    }
  }

  public static String toString(int value) {
    if( value == 1 ) {
      return "TRUE";
    } else if( value == 0) {
      return "FALSE";
    } else {
      return "NA";
    }
  }

  public int getInternalValue() {
    return internalValue;
  }
}
