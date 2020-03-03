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
      // mitigate `Method code too large` errors for large constant array expressions
      largeShortArray,
      largeIntArray,
      largeLongArray,
      largeFloatArray,
      largeDoubleArray,
      // store large string arrays either in the string pool, or in a resource for very large arrays
      stringArray,
      // store char arrays as a string in the constant pool
      charArray, // n.b., this should be _after_ `largeShortArray`
      Constructors::int32Array2d
  };
}
