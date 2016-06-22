package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.runtime.Ptr;

import javax.annotation.Nonnull;

/**
 * Realloc a void pointer
 */
public class VoidPtrRealloc implements JExpr {
  
  private final JExpr pointer;
  private final JExpr newSizeInBytes;

  public VoidPtrRealloc(JExpr pointer, JExpr newSizeInBytes) {
    this.pointer = pointer;
    this.newSizeInBytes = newSizeInBytes;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.getType(Object.class);
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    
    // We can really only meaningfully allocate Fat Pointers, so cast the 
    // Object pointer to a Ptr and invoke realloc
    
    JExpr ptr = Expressions.cast(pointer, Type.getType(Ptr.class));
    ptr.load(mv);
    
    newSizeInBytes.load(mv);
    
    mv.invokeinterface(Ptr.class, "realloc", Type.getType(Ptr.class), Type.INT_TYPE);
  }
}
