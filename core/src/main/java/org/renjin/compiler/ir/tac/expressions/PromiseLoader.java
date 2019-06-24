package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

public interface PromiseLoader {

  void load(EmitContext context, InstructionAdapter mv);
}
