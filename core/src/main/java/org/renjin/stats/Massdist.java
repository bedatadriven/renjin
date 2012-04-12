/*
 *  R : A Computer Language for Statistical Data Analysis
 *  Copyright (C) 1996-2004 Robert Gentleman and Ross Ihaka and the
 *        R Development Core Team
 *  Copyright (C) 2005    The R Foundation

 *  "HACKED" to allow weights by Adrian Baddeley
 *  Changes indicated by 'AB'
 * -------
 *  FIXME   Does he want 'COPYRIGHT' ?
 * -------
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  http://www.r-project.org/Licenses/
 */

package org.renjin.stats;

import org.renjin.sexp.DoubleVector;

class Massdist {

  static double[] massdist(double x[],
      double xmass[], 
      int nx,
      double xlow, double xhigh,
      double y[],
      int ny)
  {
    double fx, xdelta, xmi, xpos;   /* AB */
    int i, ix, ixmax, ixmin;

    
    ixmin = 0;
    ixmax = ny - 2;

    xdelta = (xhigh - xlow) / (ny - 1);

    for(i=0; i < ny ; i++)
      y[i] = 0;

    for(i=0; i < nx ; i++) {
      if(DoubleVector.isFinite(x[i])) {
        xpos = (x[i] - xlow) / xdelta;
        ix = (int)Math.floor(xpos);
        fx = xpos - ix;
        xmi = xmass[i];  
        if(ixmin <= ix && ix <= ixmax) {
          y[ix] += (1 - fx) * xmi;   
          y[ix + 1] += fx * xmi; 
        }
        else if(ix == -1) {
          y[0] += fx * xmi; 
        }
        else if(ix == ixmax + 1) {
          y[ix] += (1 - fx) * xmi; 
        }
      }
    }
    return y;
  }

}
