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

package org.renjin.primitives.random;

import org.renjin.eval.Session;

public class RLogis {

  public static double rlogis(Session context, double location, double scale) {
    if (Double.isNaN(location) || Double.isInfinite(scale)) {
      return (Double.NaN);
    }

    if (scale == 0. || Double.isInfinite(location)) {
      return location;
    } else {
      double u = context.rng.unif_rand();
      return location + scale * Math.log(u / (1. - u));
    }
  }
}
