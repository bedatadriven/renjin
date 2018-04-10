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
import org.renjin.primitives.packaging.Namespace;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;

public class S4ClassCache {

  private final Environment classTable;

  public S4ClassCache(Context context) {

    Frame classes = new HashFrame();

    List<Frame> loaded = new ArrayList<>();
    loaded.add(context.getGlobalEnvironment().getFrame());
    for(Namespace namespace : context.getNamespaceRegistry().getLoadedNamespaces()) {
      loaded.add(namespace.getNamespaceEnvironment().getFrame());
    }

    for(Frame frame : loaded) {
      for(Symbol object : frame.getSymbols()) {
        if(object.getPrintName().startsWith(".__C__")) {
          String shortName = object.getPrintName().split(".__C__")[1];
          classes.setVariable(Symbol.get(shortName), frame.getVariable(object).force(context));
        }
      }
    }

    Environment.Builder classTableBuilder = new Environment.Builder(context.getBaseEnvironment(), classes);
    this.classTable = classTableBuilder.build();
  }

  public S4Class lookupClass(String name) {
    SEXP classRepresentation = classTable.getVariableUnsafe(name);
    if(classRepresentation == Symbol.UNBOUND_VALUE) {
      return null;
    } else {
      return new S4Class(classRepresentation);
    }
  }

  public boolean needsCoerce(String from, String to) {
    SEXP classDef = classTable.getVariableUnsafe(from);
    ListVector contains = (ListVector) classDef.getAttribute(S4Class.CONTAINS);
    int index = contains.getIndexByName(to);
    if(index != -1) {
      SEXP classExtension = contains.getElementAsSEXP(index);
      SEXP simple = classExtension.getAttribute(S4Class.SIMPLE);
      return ((LogicalArrayVector) simple).isElementTrue(0);
    }
    return true;
  }

  public SEXP coerceComplex(Context context, SEXP value, String from, String to) {
    SEXP classDef = classTable.getVariableUnsafe(from);
    ListVector contains = (ListVector) classDef.getAttribute(S4Class.CONTAINS);
    int toIndex = contains.getIndexByName(to);
    if(toIndex != -1) {
      // get sloth with information about target class and create
      // new function call to perform the coercion (if "by" field is defined
      // this is an intermediate stage).
      S4Object fromClass = (S4Object) contains.getElementAsSEXP(toIndex);
      Closure coerce = (Closure) fromClass.getAttribute(S4Class.COERCE);
      FunctionCall call = new FunctionCall(coerce, new PairList.Node(value, Null.INSTANCE));

      SEXP res = context.evaluate(call);

      if(fromClass.getAttribute(S4Class.BY).length() == 0) {
        return res;
      } else {
        // the "by" field is provided. the coercion should be followed with
        // coercion provided in "by" class

        // get the "by" class information, if "by" is not in contained classes than
        // assume its a function
        String by = fromClass.getAttribute(S4Class.BY).asString();
        int byIndex = contains.getIndexByName(by);
        if(byIndex != -1) {
          S4Object byClass = (S4Object) contains.getElementAsSEXP(byIndex);
          Closure byCoerce = (Closure) byClass.getAttribute(S4Class.COERCE);
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
}
