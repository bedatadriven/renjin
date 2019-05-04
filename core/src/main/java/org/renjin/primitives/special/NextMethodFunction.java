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

package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.DispatchTable;
import org.renjin.eval.EvalException;
import org.renjin.primitives.S3;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NextMethodFunction extends BuiltinFunction {
  public NextMethodFunction() {
    super("NextMethod");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] promisedArguments, DispatchTable dispatch) {

    if(argumentNames.length > 1 || argumentNames[0] != null) {
      throw new EvalException("TODO: NextMethod arguments");
    }

    FunctionEnvironment functionEnvironment;
    try {
      functionEnvironment = (FunctionEnvironment) rho;
    } catch (ClassCastException ignored) {
      throw new EvalException("'NextMethod' called from outside a function");
    }

    DispatchTable dispatchTable = functionEnvironment.getDispatchTable();

    DispatchTable next = new DispatchTable(dispatchTable.getGenericDefinitionEnvironment(), dispatchTable.getGeneric());

    Function nextMethod = S3.findMethod(context,
        dispatchTable.getGenericDefinitionEnvironment(),
        functionEnvironment,
        dispatchTable.getGeneric(),
        dispatchTable.getGroup(),
        nextClasses(dispatchTable),
        true,
        next);

    throw new UnsupportedOperationException("TODO");
  }


  /**
   *
   * @return remaining classes to be  tried after this method
   */
  public List<String> nextClasses(DispatchTable table) {
    if(table.classVector == null) {
      return Collections.emptyList();
    }
    int classIndex = findIndex(table);
    List<String> next = new ArrayList<>();

    for (int i = classIndex + 1; i < table.classVector.length(); i++) {
      next.add(table.classVector.getElementAsString(i));
    }

    return next;
  }

  private int findIndex(DispatchTable table) {
    for (int i = 0; i < table.classVector.length(); i++) {
      String className = table.classVector.getElementAsString(i);
      if(table.method.endsWith(className)) {
        return i;
      }
    }
    return table.classVector.length() - 1;
  }
}
