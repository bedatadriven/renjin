package org.renjin.compiler.codegen;

import org.renjin.repackaged.asm.commons.InstructionAdapter;

public interface FunctionLoader {
  void loadFunction(EmitContext context, InstructionAdapter mv);
}
