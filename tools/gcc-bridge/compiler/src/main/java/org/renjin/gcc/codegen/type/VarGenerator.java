package org.renjin.gcc.codegen.type;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;


/**
 * Generates loads and stores for a variable
 */
public interface VarGenerator extends ExprGenerator {

  /**
   * Emits the bytecode for any initialization required of the variable, such
   * as heap allocations.
   * 
   * @param mv the {@code MethodVisitor} to write to
   * @param initialValue the initial value of this variable, if it has one.
   */
  void emitDefaultInit(MethodGenerator mv, Optional<ExprGenerator> initialValue);

}
