package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;

/**
 * Generates the bytecode to store values to local variables, fields, arrays, etc
 */
public interface LValueGenerator extends ExprGenerator {

  /**
   * 
   * Emits a store instruction; to a variable, to an array value, field, etc
   * 
   * @param valueGenerator the generator which produces the value to be stored
   */
  void emitStore(MethodVisitor mv, ExprGenerator valueGenerator);

}
