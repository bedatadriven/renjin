package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;

import java.util.List;

/**
 * Specializes calls to rep()
 */
public class RepSpecializer implements Specializer {
  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ValueBounds> argumentTypes) {
    // TODO: generic dispatch is squirelly for this function. See RepFunction implementation

    return UnspecializedCall.INSTANCE;

  }
}
