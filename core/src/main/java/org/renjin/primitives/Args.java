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
package org.renjin.primitives;


import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.sexp.Function;
import org.renjin.sexp.Symbol;

import java.io.IOException;

public class Args {
  
  public static Function args(@Current Context context, String name) throws IOException {
    Function function = context.getEnvironment().findFunction(context, Symbol.get(name));
    return args(context, function);
  }
  
  public static Function args(@Current Context context, Function function) throws IOException {
    return function;
  }
  
}
