package org.renjin.gcc.codegen.expr;


import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Generates bytecode for conditional jumps
 */
public interface ConditionGenerator extends ExprGenerator {
  
  void emitJump(MethodVisitor mv, Label trueLabel);
}
