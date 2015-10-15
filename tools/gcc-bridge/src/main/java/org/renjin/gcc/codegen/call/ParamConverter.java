package org.renjin.gcc.codegen.call;


import org.objectweb.asm.MethodVisitor;

/**
 * Generates bytecode to push a parameter value onto the stack
 */
public interface ParamConverter {
  
  void emitPushParam(MethodVisitor mv);
  
}
