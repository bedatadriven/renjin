package org.renjin.gcc.codegen.condition;


import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Generates bytecode for conditional jumps
 */
public interface ConditionGenerator {
  
  void emitJump(MethodVisitor mv, Label trueLabel, Label falseLabel);
}
