package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;

import java.util.List;

/**
 * Created by alex on 30-6-16.
 */
public interface Specializer {
  
  Specialization trySpecialize(RuntimeState runtimeState, List<ValueBounds> argumentTypes);
}
