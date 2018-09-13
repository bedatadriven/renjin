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
package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.guava.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

/**
 * Specializes calls to the {@code [} operator
 */
public class SubsetSpecializer implements Specializer, BuiltinSpecializer {

  private final JvmMethod method;
  private final Primitives.Entry primitive;

  public SubsetSpecializer() {
    primitive = Primitives.getBuiltinEntry("[");
    method = Iterables.getOnlyElement(JvmMethod.findOverloads(primitive.functionClass, primitive.name, primitive.methodName));
  }

  @Override
  public String getName() {
    return "[";
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {

    ArgumentBounds source = arguments.get(0);
    ArgumentBounds drop = null;

    List<ArgumentBounds> subscripts = new ArrayList<>();

    for (int i = 1; i < arguments.size(); i++) {
      ArgumentBounds argument = arguments.get(i);
      if("drop".equals(argument.getName())) {
        drop = argument;
      } else {
        subscripts.add(argument);
      }
    }

    if (subscripts.size() == 0) {
      return new CompleteSubset(source);
    }

    // If exactly two subscripts are provided
    // Such as x[i,j] or x[i,], AND
    // the source type is known, then treat this as a matrix selection
    SingleRowOrColumn singleRowOrColumn = SingleRowOrColumn.trySpecialize(source, subscripts, drop);
    if(singleRowOrColumn != null) {
      return singleRowOrColumn;
    }

    GetAtomicElement singleElement = GetAtomicElement.trySpecialize(source, subscripts);
    if(singleElement != null) {
      return singleElement;
    }

    // Call the generic runtime version....
    // Only dim, dimnames, names attributes *might* be preserved

    ValueBounds bounds = ValueBounds.builder()
        .setTypeSet(computeResultTypeSet(source))
        .addFlagsFrom(source.getBounds(), ValueBounds.MAYBE_DIM | ValueBounds.MAYBE_DIMNAMES| ValueBounds.MAYBE_NAMES)
        .build();

    return new StaticMethodCall(method, bounds);
  }

  private int computeResultTypeSet(ArgumentBounds source) {
    int set = source.getTypeSet();
    if(TypeSet.mightBe(set, TypeSet.PAIRLIST)) {
      set |= TypeSet.LIST;
      set &= ~TypeSet.PAIRLIST;
    }
    return set;
  }

}
