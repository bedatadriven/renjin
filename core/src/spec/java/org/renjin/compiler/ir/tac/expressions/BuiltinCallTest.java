package org.renjin.compiler.ir.tac.expressions;

import org.junit.Test;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.primitives.Primitives;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class BuiltinCallTest {

  
  @Test
  public void test() {
    Expression x = new EnvironmentVariable("x");
    Expression y = new EnvironmentVariable("y");

    BuiltinCall call = new BuiltinCall(Primitives.getBuiltinEntry("+"), new String[2], Arrays.asList(x, y));

    Map<Expression, ValueBounds> typeMap = new HashMap<>();
    typeMap.put(x, ValueBounds.DOUBLE_PRIMITIVE);
    typeMap.put(y, ValueBounds.DOUBLE_PRIMITIVE);

    ValueBounds bounds = call.updateTypeBounds(typeMap);

    System.out.println(bounds);
    
    assertTrue(bounds.getTypeSet() == TypeSet.DOUBLE);
    assertTrue(bounds.getLength() == 1);
  }
}