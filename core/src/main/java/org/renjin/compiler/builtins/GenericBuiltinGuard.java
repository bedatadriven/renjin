package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.sexp.Null;

import java.util.List;

/**
 * Verifies that a builtin has no S3 overloads before delegating to a
 * specializer for the default primitive method.
 */
public class GenericBuiltinGuard implements Specializer {
  
  private final Specializer specializer;

  public GenericBuiltinGuard(Specializer specializer) {
    this.specializer = specializer;
  }

  @Override
  public Specialization trySpecialize(List<ValueBounds> argumentTypes) {
    ValueBounds object = argumentTypes.get(0);
    if(object.isClassAttributeConstant()) {
      if(object.getConstantClassAttribute() == Null.INSTANCE) {
        // The argument has no class attribute, so we can safely
        // specialize the (builtin) default function.
        return specializer.trySpecialize(argumentTypes);
      }
    }
    // Can't make any assumptions. 
    return UnspecializedCall.INSTANCE;
  }
}
