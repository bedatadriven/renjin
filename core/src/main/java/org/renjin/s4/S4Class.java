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
import org.renjin.eval.EvalException;
import org.renjin.primitives.Attributes;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S4Class {



  private SEXP classRepresentation;

  private Map<String, S4Slot> slotMap = new HashMap<>();

  private Map<String, String> slots = new HashMap<>();

  public S4Class(SEXP classRepresentation) {
    this.classRepresentation = classRepresentation;

    ListVector slots = (ListVector) classRepresentation.getAttribute(Symbol.get("slots"));
    for(int i = 0; i < slots.length(); i++) {
      this.slots.put(slots.getAttribute(Symbols.NAMES).getElementAsSEXP(i).asString(), slots.getElementAsString(i));
    }
  }

  /**
   * Finds the distance between this class and the {@code otherClassName}. If {@code otherClassName} is
   * not a super class (not "contained"), this method will return -1.
   */
  public int extractDistanceFromS4Class(String otherClassName) {
    SEXP containsSexp = classRepresentation.getAttribute(S4.CONTAINS);
    return extractDistanceFromS4Class(containsSexp, otherClassName);
  }

  private int extractDistanceFromS4Class(SEXP superclassSlot, String className) {
    if(superclassSlot instanceof ListVector) {
      ListVector containsList = (ListVector) superclassSlot;
      int index = containsList.getIndexByName(className);
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
        Closure test = (Closure) classExtension.getAttribute(S4.TEST);
        if(test.getBody() instanceof LogicalArrayVector) {
          LogicalArrayVector condition = (LogicalArrayVector) test.getBody();
          if(condition.isElementTrue(0)) {
            SEXP distanceAttribute = classExtension.getAttribute(S4.DISTANCE);
            return distanceAttribute.asInt();
          }
        }
      }
    }
    return -1;
  }

  public int getDistanceToUnionClass(String className) {
    if(isUnionClass()) {
      SEXP subclasses = classRepresentation.getAttribute(S4.SUBCLASSES);
      return extractDistanceFromS4Class(subclasses, className);
    }
    return -1;
  }

  public boolean isUnionClass() {
    String objClass = classRepresentation.getAttribute(Symbols.CLASS).asString();
    return "ClassUnionRepresentation".equals(objClass);
  }

  public boolean isSimpleCoercion(String targetClass) {
    ListVector contains = (ListVector) classRepresentation.getAttribute(S4.CONTAINS);
    int index = contains.getIndexByName(targetClass);
    if(index != -1) {
      SEXP classExtension = contains.getElementAsSEXP(index);
      SEXP simple = classExtension.getAttribute(S4.SIMPLE);
      return ((LogicalArrayVector) simple).isElementTrue(0);
    }
    return true;
  }

  public SEXP coerceTo(Context context, SEXP value, String to) {
    ListVector contains = (ListVector) classRepresentation.getAttribute(S4.CONTAINS);
    int toIndex = contains.getIndexByName(to);
    if(toIndex != -1) {
      // get sloth with information about target class and create
      // new function call to perform the coercion (if "by" field is defined
      // this is an intermediate stage).
      S4Object fromClass = (S4Object) contains.getElementAsSEXP(toIndex);
      Closure coerce = (Closure) fromClass.getAttribute(S4.COERCE);
      FunctionCall call = new FunctionCall(coerce, new PairList.Node(value, Null.INSTANCE));

      SEXP res = context.evaluate(call);

      if(fromClass.getAttribute(S4.BY).length() == 0) {
        return res;
      } else {
        // the "by" field is provided. the coercion should be followed with
        // coercion provided in "by" class

        // get the "by" class information, if "by" is not in contained classes than
        // assume its a function
        String by = fromClass.getAttribute(S4.BY).asString();
        int byIndex = contains.getIndexByName(by);
        if(byIndex != -1) {
          S4Object byClass = (S4Object) contains.getElementAsSEXP(byIndex);
          Closure byCoerce = (Closure) byClass.getAttribute(S4.COERCE);
          FunctionCall byCall = new FunctionCall(byCoerce, new PairList.Node(value, Null.INSTANCE));
          return context.evaluate(byCall);
        } else {
          FunctionCall byCall = new FunctionCall(Symbol.get(by), new PairList.Node(call, Null.INSTANCE));
          return context.evaluate(byCall);
        }
      }
    }
    return value;
  }

  public SEXP getDefinition() {
    return classRepresentation;
  }

  public S4Slot getSlot(Context context, String name) {
    S4Slot slot = slotMap.get(name);
    if(slot == null) {
      slot = findSlotFromClassRepresentation(context, name);
      slotMap.put(name, slot);
    }
    return slot;
  }

  private S4Slot findSlotFromClassRepresentation(Context context, String name) {

    if(slots.containsKey(name)) {
      List<String> classNames = new ArrayList<>();

      String slotClassName = slots.get(name);
      AtomicVector validClasses = S4.computeDataClassesS4(context, slotClassName);

      classNames.add(slotClassName);
//      if(validClasses != null) {
//        for(int i = 0; i < validClasses.length(); i++) {
//          classNames.add(validClasses.getElementAsString(i));
//        }
//      }

      return new S4Slot(name, classNames);
    } else {
      throw new EvalException(name + " is not a slot in class "
          + Attributes.getClass(classRepresentation).getElementAsString(0));
    }
  }

}
