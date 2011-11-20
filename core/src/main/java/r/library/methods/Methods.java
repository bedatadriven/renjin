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

package r.library.methods;

import r.lang.Environment;
import r.lang.SEXP;
import r.lang.StringVector;

public class Methods {

  public static void R_initMethodDispatch(Environment env) {

  }

  public static void R_set_method_dispatch(boolean set) {

  }
  
  /**
   * Seems to return true if e1 and e2 are character vectors
   * both of length 1 with equal string values.
   * 
   **/
  public static boolean R_identC(SEXP e1, SEXP e2) {
    if(e1 instanceof StringVector && e2 instanceof StringVector &&
        e1.length() == 1 && e2.length() == 2) {

      StringVector s1 = (StringVector) e1;
      StringVector s2 = (StringVector) e2;
      if(!s1.isElementNA(0)) {
        return s1.getElementAsString(0).equals(s2.getElementAsString(0));
      }

    }
    return false;
  }
}
