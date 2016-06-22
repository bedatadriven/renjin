package org.renjin.gcc.codegen.type.primitive.op;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;

import javax.annotation.Nonnull;

import static org.objectweb.asm.Type.INT_TYPE;


public class BitwiseLRotateGenerator implements JExpr {
  
  private JExpr bits;
  private JExpr k;

  public BitwiseLRotateGenerator(JExpr bits, JExpr k) {
    this.bits = bits;
    this.k = k;
    Preconditions.checkArgument(bits.getType() == Type.INT_TYPE);
  }

  @Nonnull
  @Override
  public Type getType() {
    return bits.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    
    //(bits >>> k) | (bits << (Integer.SIZE - k));


//    0: iload_0
//    1: iload_1
//    2: iushr
    bits.load(mv);
    k.load(mv);
    mv.ushr(bits.getType());
    
//    3: iload_0
//    4: bipush        32
//    6: iload_1
//    7: isub
//    8: ishl
//    9: ior
    bits.load(mv);
    mv.iconst(32);
    k.load(mv);
    mv.sub(INT_TYPE);
    mv.shl(INT_TYPE);
    mv.or(INT_TYPE);
  }

}
