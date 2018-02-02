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
package org.renjin.s4;

public class DistanceCalculator {

  private static final int FAR_AWAY = Integer.MAX_VALUE - 1;

  private S4ClassCache classCache;

  public DistanceCalculator(S4ClassCache classCache) {
    this.classCache = classCache;
  }

  public int distance(String from, String to) {
    if (to.equals("ANY")) {
      // Classes are equidistant but far from "ANY"
      // ... but missing is even further.
      if(from.equals("missing")) {
        return FAR_AWAY + 1;
      } else {
        return FAR_AWAY;
      }
    }

    S4Class providedClass = classCache.lookupClass(from);
    if(providedClass == null) {
      return -1;
    }

    int distanceToSuperClass = providedClass.getDistanceToSuperClass(to);
//    System.out.println(String.format("%s => %s = %d", from, to, distanceToSuperClass));
    return distanceToSuperClass;
  }
}
