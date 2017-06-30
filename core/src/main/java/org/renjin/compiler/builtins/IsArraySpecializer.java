/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.gcc.runtime.Builtins;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Types;
import org.renjin.repackaged.guava.collect.Iterables;

import java.util.List;

/**
 * Specializes calls to the {@code is.array} primitive.
 */
public class IsArraySpecializer implements BuiltinSpecializer {
  
  private JvmMethod method;

  public IsArraySpecializer() {
    this.method = Iterables.getOnlyElement(JvmMethod.findOverloads(Types.class, "is.array", null));
  }

  @Override
  public String getName() {
    return "is.array";
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ValueBounds> argumentTypes) {
    if(argumentTypes.size() != 1) {
      throw new InvalidSyntaxException("is.array() takes one argument.");
    }

    ValueBounds argumentBounds = argumentTypes.get(0);
    if(argumentBounds.isDimCountConstant()) {
      return new ConstantCall(argumentBounds.getConstantDimCount() > 0);
    }

    return new StaticMethodCall(method);
  }

}
