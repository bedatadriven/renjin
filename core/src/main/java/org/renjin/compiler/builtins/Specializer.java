package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.ValueBounds;

import java.util.List;

/**
 * Created by alex on 30-6-16.
 */
public interface Specializer {
  
  Specialization trySpecialize(List<ValueBounds> argumentTypes);
}
