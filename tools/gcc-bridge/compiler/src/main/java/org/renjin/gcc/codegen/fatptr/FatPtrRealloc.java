package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.runtime.Realloc;

import javax.annotation.Nonnull;

/**
 * Invokes a type-specific realloc() method to enlarge the new array
 */
public class FatPtrRealloc implements JExpr {

  private FatPtrExpr pointer;
  private JExpr newLength;
  private Type arrayType;

  public FatPtrRealloc(FatPtrExpr pointer, JExpr newLength) {
    this.pointer = pointer;
    this.newLength = newLength;
    arrayType = pointer.getArray().getType();
  }

  @Nonnull
  @Override
  public Type getType() {
    return arrayType;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {

    pointer.getArray().load(mv);
    pointer.getOffset().load(mv);
    newLength.load(mv);
    
    mv.invokestatic(Realloc.class, "realloc", Type.getMethodDescriptor(arrayType, arrayType, Type.INT_TYPE, Type.INT_TYPE));
  }
}
