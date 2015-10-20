package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IDIV;

/**
 * Generates code for pointer addition
 */
public class PtrPlusGenerator extends AbstractExprGenerator implements PtrGenerator {
  
  private PtrGenerator ptr;
  private ValueGenerator offset;

  public PtrPlusGenerator(ExprGenerator ptr, ExprGenerator offset) {
    this.ptr = (PtrGenerator) ptr;
    this.offset = (ValueGenerator) offset;
  }

  @Override
  public void emitPushArrayAndOffset(MethodVisitor mv) {
    ptr.emitPushArrayAndOffset(mv);

    // Now add the delta to the offset on the stack and add
    // it to the current offset
    pushDelta(mv);
    mv.visitInsn(IADD);
  }

  private void pushDelta(MethodVisitor mv) {
    int sizeInBytes = ptr.getGimpleType().getBaseType().sizeOf();

    if(offset instanceof ConstValueGenerator) {
      // If the pointer is incremented by a constant amount, we can calculate the offset now
      ((ConstValueGenerator) offset).divideBy(sizeInBytes).emitPushValue(mv);
      
    } else {
      // Otherwise we have to compute the offset at runtime
      offset.emitPushValue(mv);
      new ConstValueGenerator(new GimpleIntegerType(32), sizeInBytes).emitPushValue(mv);
      mv.visitInsn(IDIV);
    }
  }

  @Override
  public GimpleType getGimpleType() {
    return ptr.getGimpleType();
  }
}
