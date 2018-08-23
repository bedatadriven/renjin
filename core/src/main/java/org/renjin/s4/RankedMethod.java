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

import org.renjin.eval.Context;
import org.renjin.sexp.Closure;

import java.util.Arrays;

public class RankedMethod {

  private final Method method;
  private boolean has0;
  private boolean candidate = true;
  private boolean exact = true;
  private int[] distances;

  public RankedMethod(Context context, Method method, Signature callingSignature, DistanceCalculator distanceCalculator, boolean[] useInheritance) {
    this.method = method;
    this.distances = new int[getMethodSignatureLength()];
    for (int i = 0; i < getMethodSignatureLength(); i++) {
      String definedClass = method.getSignature().getClass(i);
      String targetClass = callingSignature.getClass(i);
      if (definedClass.equals(targetClass)) {
        // matches exactly
        distances[i] = 0;
        has0 = true;

      } else if(useInheritance[i]) {
        // allowed to inherit
        exact = false;
        int distance = distanceCalculator.distance(context, targetClass, definedClass);
        if (distance == -1) {
          candidate = false;
          break;
        }
        distances[i] = distance;
      } else {
        candidate = false;
        break;
      }
    }
  }

  public boolean isCandidate() {
    return candidate;
  }

  /**
   * @return {@code true} if the calling signature is a perfect class-by-class match for this
   * {@link Method}
   */
  public boolean isExact() {
    return exact;
  }

  public boolean isBetterThan(RankedMethod other) {


    // Methods with one exact match are better than methods with no exact matches
    if (this.has0 && !other.has0) {
      return true;
    }
    if (!this.has0 && other.has0) {
      return false;
    }

    // Otherwise, we compare the distances, starting from the first argument
    for (int i = 0; i < distances.length && i < other.distances.length; i++) {
      if (this.distances[i] < other.distances[i]) {
        return true;
      } else if (this.distances[i] > other.distances[i]) {
        return false;
      }
    }

    // true: All distances being equal, specific methods are better than group methods
    // false: The methods are equal or group methods better than specific methods...
    return this.method.getSpecificity() < other.method.getSpecificity();
  }

  public Method getMethod() {
    return method;
  }

  public Closure getMethodDefinition() {
    return method.getDefinition();
  }

  @Override
  public String toString() {
    return "RankedMethod{" +
        "method=" + method +
        ", has0=" + has0 +
        ", candidate=" + candidate +
        ", exact=" + exact +
        ", distances=" + Arrays.toString(distances) +
        '}';
  }

  public String getArgumentClass(int index) {
    return method.getSignature().getClass(index);
  }

  public int getMethodSignatureLength() {
    return method.getSignature().getLength();
  }
}
