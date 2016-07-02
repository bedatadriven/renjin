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

    BuiltinCall call = new BuiltinCall(functionCall, Primitives.getBuiltinEntry("+"), 
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

    BuiltinCall call = new BuiltinCall(functionCall, Primitives.getBuiltinEntry("+"), 
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