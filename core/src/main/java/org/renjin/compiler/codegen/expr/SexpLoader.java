package org.renjin.compiler.codegen.expr;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

public interface SexpLoader {
  /**
   * Writes the bytecode to load this expression onto the stack as an {@code SEXP} object.
   */
  void loadSexp(EmitContext context, InstructionAdapter mv);
}
