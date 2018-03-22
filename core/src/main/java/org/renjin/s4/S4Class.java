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

import org.renjin.sexp.*;

public class S4Class {

  public static final Symbol CONTAINS = Symbol.get("contains");
  public static final Symbol DISTANCE = Symbol.get("distance");
  public static final Symbol COERCE = Symbol.get("coerce");
  public static final Symbol REPLACE = Symbol.get("replace");
  public static final Symbol BY = Symbol.get("by");
  public static final Symbol SIMPLE = Symbol.get("simple");
  public static final Symbol TEST = Symbol.get("test");

  private SEXP classRepresentation;

  public S4Class(SEXP classRepresentation) {
    this.classRepresentation = classRepresentation;
  }

  /**
   * Finds the distance between this class and the {@code otherClassName}. If {@code otherClassName} is
   * not a super class (not "contained"), this method will return -1.
   */
  public int getDistanceToSuperClass(String otherClassName) {
    SEXP containsSexp = classRepresentation.getAttribute(CONTAINS);
    if(containsSexp instanceof ListVector) {
      ListVector containsList = (ListVector) containsSexp;
      int index = containsList.getIndexByName(otherClassName);
      if(index != -1) {
        SEXP classExtension = containsList.getElementAsSEXP(index);

        /*
         * when using setIs a conditional inheritance can be provided with "test" argument.
         * setClass("A")
         * setClass("B", representation(x="numeric"))
         * setIs("B","A", test=function(x) x > 100 )
         *
         * The function provided to "test" argument is stored in "test" field of class "B".
         * The default value for the "test" field is TRUE. If there is an conditional
         * inheritance defined in "test" field than this inheritence is not considered for
         * method dispatch
         */
        Closure test = (Closure) classExtension.getAttribute(S4Class.TEST);
        if(test.getBody() instanceof LogicalArrayVector) {
          LogicalArrayVector condition = (LogicalArrayVector) test.getBody();
          if(condition.isElementTrue(0)) {
            SEXP distanceAttribute = classExtension.getAttribute(DISTANCE);
            return distanceAttribute.asInt();
          }
        }
      }
    }
    return -1;
  }
}
