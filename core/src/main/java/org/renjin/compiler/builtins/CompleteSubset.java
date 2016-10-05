package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

/**
 * Created by alex on 29-9-16.
 */
public class CompleteSubset implements Specialization {

  private ValueBounds sourceBounds;

  public CompleteSubset(ValueBounds sourceBounds) {
    this.sourceBounds = sourceBounds;
  }

  @Override
  public Type getType() {
    return sourceBounds.storageType();
  }

  public ValueBounds getResultBounds() {
    return sourceBounds;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    arguments.get(0).getExpression().load(emitContext, mv);
  }
}
