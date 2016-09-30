package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.primitives.subset.MatrixSelection;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

/**
 * Specialization of a subset operation known to be a matrix subset.
 */
public class MatrixSubset implements Specialization {
  private final ValueBounds source;
  private final List<ValueBounds> subscripts;
  private ValueBounds result;

  private boolean drop = true;
  
  public MatrixSubset(ValueBounds source, List<ValueBounds> subscripts) {
    this.source = source;
    this.subscripts = subscripts;
    this.result = MatrixSelection.computeResultBounds(source, subscripts, true);
  }


  /**
   * Try to infer result length, dimensions, etc.
   */
  public Specialization tryFurtherSpecialize() {
    return this;
  }

  @Override
  public Type getType() {
    return result.storageType();
  }

  @Override
  public ValueBounds getValueBounds() {
    return result;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    throw new UnsupportedOperationException();
  }
}
