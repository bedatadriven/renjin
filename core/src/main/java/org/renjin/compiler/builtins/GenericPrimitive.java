package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.SEXP;

import java.util.List;


public class GenericPrimitive implements Specialization {
  
  public static final GenericPrimitive INSTANCE = new GenericPrimitive();
  
  private GenericPrimitive() { }
  
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
    throw new FailedToSpecializeException("generic dispatch from primitives not yet implemented.");
  }
}
