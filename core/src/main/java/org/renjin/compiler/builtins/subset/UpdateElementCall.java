package org.renjin.compiler.builtins.subset;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.builtins.Specialization;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;

import java.util.List;

/**
 * Updates a single element in an atomic vector with a new scalar value.
 */
public class UpdateElementCall implements Specialization {
  
  private ValueBounds inputVector;
  private ValueBounds subscript;
  private ValueBounds replacement;

  public UpdateElementCall(ValueBounds inputVector, ValueBounds subscript, ValueBounds replacement) {
    this.inputVector = inputVector;
    this.subscript = subscript;
    this.replacement = replacement;
  }

  @Override
  public Type getType() {
    return inputVector.storageType();
  }

  @Override
  public ValueBounds getValueBounds() {
    return ValueBounds.vector(inputVector.getTypeSet(), inputVector.getLength());
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    throw new UnsupportedOperationException();
  }
}
