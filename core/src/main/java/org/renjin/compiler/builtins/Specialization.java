package org.renjin.compiler.builtins;


import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.Expression;

import java.util.List;

public interface Specialization {
  
  Type getType();

  ValueBounds getValueBounds();

  void load(EmitContext emitContext, InstructionAdapter mv, List<Expression> arguments);

}
