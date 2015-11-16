package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.gimple.type.GimpleType;


public class BitwiseLRotateGenerator extends AbstractExprGenerator {
  
  private ExprGenerator bits;
  private ExprGenerator k;

  public BitwiseLRotateGenerator(ExprGenerator bits, ExprGenerator k) {
    this.bits = bits;
    this.k = k;
  }

  @Override
  public GimpleType getGimpleType() {
    return bits.getGimpleType();
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    
    //(bits >>> k) | (bits << (Integer.SIZE - k));


//    0: iload_0
//    1: iload_1
//    2: iushr
    bits.emitPrimitiveValue(mv);
    k.emitPrimitiveValue(mv);
    mv.visitInsn(Opcodes.IUSHR);

//    3: iload_0
//    4: bipush        32
//    6: iload_1
//    7: isub
//    8: ishl
//    9: ior
    bits.emitPrimitiveValue(mv);
    mv.visitIntInsn(Opcodes.BIPUSH, 32);
    k.emitPrimitiveValue(mv);
    mv.visitInsn(Opcodes.ISUB);
    mv.visitInsn(Opcodes.ISHL);
    mv.visitInsn(Opcodes.IOR);
  }
}
