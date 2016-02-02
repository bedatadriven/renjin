package org.renjin.gcc.codegen.var;

import org.renjin.gcc.codegen.MethodGenerator;

/**
 * Generic interface to generation of load/stores for either local variables or static fields
 */
public interface Var extends Value {
  
  
  void store(MethodGenerator mv, Value value);
}
