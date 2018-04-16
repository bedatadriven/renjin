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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    int distanceToSuperClass = providedClass.extractDistanceFromS4Class(to);
//    System.out.println(String.format("%s => %s = %d", from, to, distanceToSuperClass));


    // inheritances distance to the "to" class are defined in the "contains" slot of providedClass.
    // In addition, inheritance can be defined using "setUnionClass", in which case a new class
    // is created with the provided name. The members of the class of a distance of "1" to each other
    // and a distance of 2 or more to superclasses of members
    if(distanceToSuperClass == -1) {
//    if(false) {
      // First lookup which union classes include the class of input
      // Loop through each union class and lookup the distance
      Iterator<String> iterator = classCache.getUnionClasses(from);
      List<String> unions = new ArrayList<>();
      while(iterator.hasNext()) {
        unions.add(iterator.next());
      }

      if(unions.contains(to)) {
        // distance of "input class" to "union class" containing the input class is always 1
        distanceToSuperClass = 1;
      }
    }

    return distanceToSuperClass;
  }
}
