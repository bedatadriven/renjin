package org.renjin.gcc.codegen.condition;


import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.repackaged.asm.Label;

/**
 * Generates bytecode for conditional jumps
 */
public interface ConditionGenerator {
  
  void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel);
}
