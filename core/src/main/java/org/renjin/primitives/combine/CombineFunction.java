/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.primitives.combine;

import org.renjin.eval.Context;
import org.renjin.invoke.codegen.WrapperRuntime;
import org.renjin.sexp.*;

public class CombineFunction extends BuiltinFunction {
  public CombineFunction() {
    super("c");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] arguments) {
    boolean recursive = false;
    int inputCount = 0;
    for (int i = 0; i < argumentNames.length; i++) {
      SEXP argument = arguments[i].force(context);
      String argumentName = argumentNames[i];
      if("recursive".equals(argumentName)) {
        recursive = WrapperRuntime.convertToBooleanPrimitive(argument);
      } else {
        argumentNames[inputCount] = argumentName == null ? "" : argumentName;
        arguments[inputCount] = argument;
        inputCount++;
      }
    }

    ListVector.NamedBuilder list = ListVector.newNamedBuilder();
    for (int i = 0; i < inputCount; i++) {
      list.add(argumentNames[i], arguments[i]);
    }

    return Combine.c(list.build(), recursive);

  }
}
