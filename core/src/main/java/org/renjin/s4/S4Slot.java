/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.eval.EvalException;
import org.renjin.methods.Methods;
import org.renjin.primitives.Attributes;
import org.renjin.sexp.*;

import java.util.*;

public class S4Slot {
  private String name;
  private List<String> validAssignmentClasses;

  public S4Slot(String name, List<String> validAssignmentClasses) {
    this.name = name;
    this.validAssignmentClasses = validAssignmentClasses;
  }

  public void checkAssignment(Context context, String valueClass) {
    if(validAssignmentClasses.contains(valueClass) || validAssignmentClasses.contains("ANY")) {
      return;
    }

    doCheckAssignment(context, valueClass);

    validAssignmentClasses.add(valueClass);
  }

  private void doCheckAssignment(Context context, String valueClass) {

    // do similar checking as in RClassUtils.R -> .possibleExtends()

    String slotClass = validAssignmentClasses.get(0);

    StringVector class1 = StringVector.valueOf(valueClass);
    SEXP classDef1 = Methods.getClassDef(context, class1, Null.INSTANCE, Null.INSTANCE, true);
    StringVector valueSuperclasses = Attributes.getSlotElementsNames(classDef1, S4.CONTAINS);
    StringVector valueSubclasses = Attributes.getSlotElementsNames(classDef1, S4.SUBCLASSES);

    StringVector class2 = StringVector.valueOf(slotClass);
    SEXP classDef2 = Methods.getClassDef(context, class2, Null.INSTANCE, Null.INSTANCE, true);

    if(classDef2 == null) {
      throw new EvalException("assignment of an object of class " + valueClass
          + " is not valid for @" + this.name
          + " in an object of class %s; is(value, \"" + slotClass
          + "\") is not TRUE");
    }

    if(Arrays.asList(valueSuperclasses.toArray()).contains(slotClass)) {
      return;
    } else {

      if(valueSubclasses.length() > 0 || valueSuperclasses.length() > 0) {
        String classDef2Class = ((StringVector) classDef2.getAttribute(Symbols.CLASS)).getElementAsString(0);
        if(!"classRepresenation".equals(classDef2Class) && "ClassUnionRepresentation".equals(classDef2Class)) {
          List<String> allClasses = new ArrayList<>();
          allClasses.add(valueClass);
          allClasses.addAll(Arrays.asList(valueSuperclasses.toArray()));
          StringVector slotSubclasses = Attributes.getSlotElementsNames(classDef2, S4.SUBCLASSES);
          if(slotSubclasses.length() > 0) {
            allClasses.addAll(Arrays.asList(slotSubclasses.toArray()));
          }

          if(hasDuplicate(allClasses)) {
            return;
          }

        }
      } else {
        SEXP slotclassContains = classDef2.getAttribute(S4.CONTAINS);
        if(slotclassContains != null) {
          StringVector slotclassNames = (StringVector) slotclassContains.getAttribute(Symbols.NAMES);
          if(slotclassNames != null) {
            List<String> namesList = Arrays.asList(slotclassNames.toArray());
            if(namesList.contains(valueClass)) {
              return;
            }
          }
        }
      }

    }

    // remove conditional and throw error if all previous conditions have failed
    if(!(slotClass.equals("ANY") || validAssignmentClasses.contains(valueClass))) {
      throw new EvalException("invalid class object: invalid object for slot \"" + name
          + "\": got class \"" + valueClass
          + "\", should be or extend class \"" + slotClass + "\"");
    }
  }

  private <T> boolean hasDuplicate(Iterable<T> all) {
    Set<T> set = new HashSet<T>();
    for (T each: all) {
      if (!set.add(each)) {
        return true;
      }
    }
    return false;
  }


}
