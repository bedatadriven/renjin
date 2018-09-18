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

import org.renjin.compiler.ir.NamedShape;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.sexp.IntVector;

import java.util.List;


public class LengthSpecializer implements Specializer, BuiltinSpecializer {


  @Override
  public String getName() {
    return "length";
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {
    if(arguments.size() != 1) {
      throw new InvalidSyntaxException("length() takes one argument.");
    }

    ArgumentBounds argument = arguments.get(0);
    ValueBounds argumentBounds = argument.getBounds();

    if(argumentBounds.isConstant()) {
      return new ConstantCall(IntVector.valueOf(argumentBounds.getConstantValue().length()));
    }

    if(argumentBounds.isFlagSet(ValueBounds.LENGTH_ONE)) {
      return new ConstantCall(IntVector.valueOf(1));
    }

    if(argumentBounds.getShape() instanceof NamedShape) {
      return new ConstantCall(IntVector.valueOf(((NamedShape) argumentBounds.getShape()).getLength()));
    }
    
    return new LengthCall(argument);
  }

}
