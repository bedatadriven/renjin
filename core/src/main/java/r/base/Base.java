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

import r.jvmi.annotations.Current;
import r.lang.*;
import r.lang.exception.EvalException;

/**
 * Implementation of routines from the base dll.
 */
public class Base {

  private Base() { }

  public static boolean R_isMethodsDispatchOn(@Current Context context) {
    return false;
  }

  public static ListVector R_getSymbolInfo(String sname, SEXP spackage, boolean withRegistrationInfo) {

    ListVector.Builder result = new ListVector.Builder();
    result.setAttribute(Symbol.CLASS, new StringVector("CRoutine"));

    return result.build();

  }

  public static ListVector R_getRegisteredRoutines(String dll) {
    ListVector.Builder builder = new ListVector.Builder();
    return builder.build();
  }

  /** @return  n if the data frame 'vec' has c(NA, n) rownames;
   *         nrow(.) otherwise;  note that data frames with nrow(.) == 0
   *          have no row.names.
   * ==> is also used in dim.data.frame()
   *
   * AB Note: I have no idea what this function really does but it is
   * so opaque that it is first against the wall when the revolution comes.
   */
  public static SEXP R_shortRowNames(SEXP vector, int type) {
    SEXP s =  vector.getAttribute(Symbol.ROW_NAMES);
    SEXP ans = s;

    if( type < 0 || type > 2) {
      throw new EvalException("invalid 'type' argument");
    }

    if(type >= 1) {
      int n;
      if (s instanceof IntVector && s.length() == 2 && ((IntVector) s).isElementNA(0)) {
        n = ((IntVector) s).getElementAsInt(1);
      } else {
        if (s == Null.INSTANCE) {
          n = 0;
        } else {
          n = s.length();
        }
      }
      if (type == 1) {
        ans = new IntVector(n);
      } else {
        ans = new IntVector(Math.abs(n));
      }
    }
    return ans;
  }
}
