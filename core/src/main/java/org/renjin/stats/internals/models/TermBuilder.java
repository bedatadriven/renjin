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
package org.renjin.stats.internals.models;


import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.List;

public class TermBuilder {
  
  private List<SEXP> expressions = Lists.newArrayList();
  public static final Symbol COLON = Symbol.get(":");
  public static final Symbol I = Symbol.get("I");
  
  public Term build(SEXP expr) {
    add(expr);
    return new Term(expressions);
  }

  private void add(SEXP expr) {
    if(expr instanceof FunctionCall) {
      call((FunctionCall)expr);
    } else {
      expressions.add(expr);
    }
  }

  private void call(FunctionCall call) {
    if(call.getFunction() == COLON) {
      add(call.getArgument(0));
      add(call.getArgument(1));
    } else {
      // treat all other calls as arithmetic
      expressions.add(call);
    }
  }
}
