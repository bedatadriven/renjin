package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.runtime.MallocThunk;

import javax.annotation.Nonnull;

/**
 * Generates a new {@code MallocThunk} instance that will allocate the requested
 * memory when first cast to a concrete type.
 */
public class NewMallocThunkExpr implements JExpr {
  
  private JExpr sizeInBytes;

  public NewMallocThunkExpr(JExpr sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.getType(MallocThunk.class);
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    mv.anew(Type.getType(MallocThunk.class));
    mv.dup();
    sizeInBytes.load(mv);
    mv.invokeconstructor(Type.getType(MallocThunk.class), Type.INT_TYPE);
  }
}
