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
package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArgumentBounds {
  private String name;
  private ValueBounds bounds;

  public ArgumentBounds(String name, ValueBounds bounds) {
    this.name = name;
    this.bounds = bounds;
  }

  public ArgumentBounds(ValueBounds valueBounds) {
    this.name = null;
    this.bounds = valueBounds;
  }

  public String getName() {
    return name;
  }

  public ValueBounds getBounds() {
    return bounds;
  }

  /**
   * Combine a symbolic argument list with the current typeMap from the TypeSolver to get a list of
   * named arguments and their ValueBounds.
   *
   */
  public static List<ArgumentBounds> create(List<IRArgument> arguments, Map<Expression, ValueBounds> typeMap) {
    List<ArgumentBounds> result = new ArrayList<>();
    for (int i = 0; i < arguments.size(); i++) {
      IRArgument symbolArgument = arguments.get(i);

      Expression argumentExpr = symbolArgument.getExpression();

      // Lookup the value bounds of this symbolic expression in the typeMap,
      // which tell us the bounds of this value at the *current* loop interation of the TypeSolver
      ValueBounds argumentBounds;
      if(argumentExpr instanceof Constant) {
        argumentBounds = argumentExpr.getValueBounds();
      } else {
        argumentBounds = typeMap.get(argumentExpr);
      }
      assert argumentBounds != null : "No argument bounds for " + symbolArgument.getName();

      result.add(new ArgumentBounds(symbolArgument.getName(), argumentBounds));
    }
    return result;
  }

  public static List<ValueBounds> withoutNames(List<ArgumentBounds> argumentBounds) {
    List<ValueBounds> values = new ArrayList<>();
    for (ArgumentBounds argumentBound : argumentBounds) {
      values.add(argumentBound.getBounds());
    }
    return values;
  }
}
