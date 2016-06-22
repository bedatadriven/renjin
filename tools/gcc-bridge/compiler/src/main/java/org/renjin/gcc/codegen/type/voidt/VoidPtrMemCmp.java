package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.gcc.runtime.VoidPtr;

import javax.annotation.Nonnull;

/**
 * Compares two pointers of unknown type by delegating to 
 * {@link ObjectPtr#memcmp(Object, Object, int)}
 */
public class VoidPtrMemCmp implements JExpr {
  
  private JExpr x;
  private JExpr y;
  private JExpr n;

  public VoidPtrMemCmp(JExpr x, JExpr y, JExpr n) {
    this.x = x;
    this.y = y;
    this.n = n;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.INT_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    x.load(mv);
    y.load(mv);
    n.load(mv);
    mv.invokestatic(VoidPtr.class, "memcmp", 
        Type.getMethodDescriptor(Type.INT_TYPE, 
            Type.getType(Object.class), Type.getType(Object.class), Type.INT_TYPE));
  }
}
