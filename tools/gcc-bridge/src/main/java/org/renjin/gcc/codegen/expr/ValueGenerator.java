package org.renjin.gcc.codegen.expr;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public interface ValueGenerator extends ExprGenerator {

  /**
   * 
   * @return the type of primitive generated 
   */
  Type primitiveType();

  /**
   * Writes the code to push this primiitive value on the stack.
   */
  void emitPush(MethodVisitor mv);
  
}
