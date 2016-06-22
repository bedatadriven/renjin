package org.renjin.gcc.codegen.condition;


import org.objectweb.asm.Label;
import org.renjin.gcc.codegen.MethodGenerator;

/**
 * Generates bytecode for conditional jumps
 */
public interface ConditionGenerator {
  
  void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel);
}
