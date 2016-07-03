package org.renjin.compiler.builtins;


import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

public interface Specialization {
  
  Type getType();

  ValueBounds getValueBounds();

  void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments);
  

}
