package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.ExprGenerator;


/**
 * Generates loads and stores for a local variable
 */
public interface VarGenerator extends ExprGenerator {

  /**
   * Emits the bytecode for any initialization required of the variable, such
   * as heap allocations.
   * 
   * @param mv the {@code MethodVisitor} to write to
   * @param initialValue the initial value of this variable, if it has one.
   */
  void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue);

  void emitDebugging(MethodVisitor mv, String name, Label start, Label end);
}
