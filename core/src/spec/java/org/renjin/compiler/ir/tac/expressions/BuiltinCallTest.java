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
package org.renjin.compiler.ir.tac.expressions;

import org.junit.Test;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class BuiltinCallTest {

  
  @Test
  public void testDoublePlusDouble() {

    FunctionCall functionCall = FunctionCall.newCall(
        Symbol.get("+"), Symbol.get("x"), Symbol.get("y"));

    Expression x = new EnvironmentVariable("x");
    Expression y = new EnvironmentVariable("y");

    BuiltinCall call = new BuiltinCall(null, functionCall, Primitives.getBuiltinEntry("+"), 
        Arrays.asList( new IRArgument(x), new IRArgument(y)));

    Map<Expression, ValueBounds> typeMap = new HashMap<>();
    typeMap.put(x, ValueBounds.DOUBLE_PRIMITIVE);
    typeMap.put(y, ValueBounds.DOUBLE_PRIMITIVE);

    ValueBounds bounds = call.updateTypeBounds(typeMap);

    System.out.println(bounds);
    
    assertTrue(bounds.getTypeSet() == TypeSet.DOUBLE);
    assertTrue(bounds.getLength() == 1);
  }

  @Test
  public void testDoublePlusInt() {


    FunctionCall functionCall = FunctionCall.newCall(
        Symbol.get("+"), Symbol.get("x"), Symbol.get("y"));

    
    Expression x = new EnvironmentVariable("x");
    Expression y = new EnvironmentVariable("y");

    BuiltinCall call = new BuiltinCall(null, functionCall, Primitives.getBuiltinEntry("+"), 
        Arrays.asList( new IRArgument(x), new IRArgument(y) ));

    Map<Expression, ValueBounds> typeMap = new HashMap<>();
    typeMap.put(x, ValueBounds.DOUBLE_PRIMITIVE);
    typeMap.put(y, ValueBounds.INT_PRIMITIVE);

    ValueBounds bounds = call.updateTypeBounds(typeMap);

    System.out.println(bounds);

    assertTrue(bounds.getTypeSet() == TypeSet.DOUBLE);
    assertTrue(bounds.getLength() == 1);
  }
}