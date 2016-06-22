package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;

import javax.annotation.Nonnull;

import static org.objectweb.asm.Type.INT_TYPE;

/**
 * Implementation of memcpy() for {@code FatPtr}s
 */
public class FatPtrMemCmp implements JExpr {

  private FatPtrExpr p1;
  private FatPtrExpr p2;
  private JExpr n;

  public FatPtrMemCmp(FatPtrExpr p1, FatPtrExpr p2, JExpr n) {
    this.p1 = p1;
    this.p2 = p2;
    this.n = n;
  }

  @Nonnull
  @Override
  public Type getType() {
    return INT_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    p1.getArray().load(mv);
    p1.getOffset().load(mv);
    p2.getArray().load(mv);
    p2.getOffset().load(mv);
    n.load(mv);

    Type valueType = p1.getValueType();
    Type arrayType = Wrappers.valueArrayType(valueType);
    Type wrapperType = Wrappers.wrapperType(valueType);
    
    // Each wrapper type (IntPtr, DoublePtr, etc) defines a static memcmp() with the the signature,
    // for example, memcmp(double[] a1, int offset1, double[] array2, int offset2, int byteCount)
    String signature = Type.getMethodDescriptor(INT_TYPE, arrayType, INT_TYPE, arrayType, INT_TYPE, INT_TYPE);
    
    mv.invokestatic(wrapperType.getInternalName(), "memcmp", signature, false);
  }
}
