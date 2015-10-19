package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.LValueGenerator;


/**
 * Generates loads and stores for a local variable
 */
public interface VarGenerator extends LValueGenerator {

  void emitDefaultInit(MethodVisitor mv);
}
