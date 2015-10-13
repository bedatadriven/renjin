package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;

/**
 * Code generator that can store values
 */
public interface LValueGenerator {

  /**
   * 
   * Emits a store instruction; to a variable, to an array value, field, etc
   * 
   * @param valueGenerator the generator which produces the value to be stored
   */
  void emitStore(MethodVisitor mv, ExprGenerator valueGenerator);

}
