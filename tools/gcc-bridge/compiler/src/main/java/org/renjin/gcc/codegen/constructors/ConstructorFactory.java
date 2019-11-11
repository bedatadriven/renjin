package org.renjin.gcc.codegen.constructors;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.ResourceWriter;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.gimple.expr.GimpleConstructor;

import static org.renjin.gcc.codegen.constructors.Constructors.*;

public class ConstructorFactory {
  public static GExpr tryCreate(MethodGenerator mv, ResourceWriter resourceWriter, GimpleConstructor value) {
    for (ConstructorInterface instance : instances) {
      GExpr result = instance.tryCreate(mv, resourceWriter, value);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private static Constructors.ConstructorInterface[] instances = {
      largeShortArray,
      largeDoubleArray,
      stringArray,
      charArray
  };
}
