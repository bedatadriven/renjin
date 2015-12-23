package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;

/**
 * Generic interface to generation of load/stores for either local variables or static fields
 */
public interface Var {
  
  void load(MethodVisitor mv);
  
  void store(MethodVisitor mv);
}
