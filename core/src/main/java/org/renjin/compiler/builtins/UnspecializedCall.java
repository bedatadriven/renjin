package org.renjin.compiler.builtins;


import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.sexp.SEXP;

import java.util.List;

public class UnspecializedCall implements Specialization {

  public static final UnspecializedCall INSTANCE = new UnspecializedCall();
  
  private UnspecializedCall() {
  }

  @Override
  public Type getType() {
    return Type.getType(SEXP.class);
  }

  @Override
  public ValueBounds getValueBounds() {
    return ValueBounds.UNBOUNDED;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    throw new FailedToSpecializeException();
  }
}
