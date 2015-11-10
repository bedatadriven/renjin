package org.renjin.gcc.codegen.var;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.ExprGenerator;


/**
 * Generates loads and stores for a local variable
 */
public interface VarGenerator extends ExprGenerator {

  void emitDefaultInit(MethodVisitor mv);

  void emitDebugging(MethodVisitor mv, String name, Label start, Label end);
}
