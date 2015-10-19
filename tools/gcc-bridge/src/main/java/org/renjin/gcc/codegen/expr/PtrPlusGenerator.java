package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IDIV;

/**
 * Generates code for pointer addition
 */
public class PtrPlusGenerator implements PtrGenerator {
  
  private PtrGenerator ptr;
  private ValueGenerator offset;

  public PtrPlusGenerator(ExprGenerator ptr, ExprGenerator offset) {
    this.ptr = (PtrGenerator) ptr;
    this.offset = (ValueGenerator) offset;
  }

  @Override
  public GimpleType gimpleBaseType() {
    return ptr.gimpleBaseType();
  }

  @Override
  public Type baseType() {
    return ptr.baseType();
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
    int sizeInBytes = ptr.gimpleBaseType().sizeOf();

    if(offset instanceof ConstValueGenerator) {
      // If the pointer is incremented by a constant amount, we can calculate the offset now
      ((ConstValueGenerator) offset).divideBy(sizeInBytes).emitPush(mv);
      
    } else {
      // Otherwise we have to compute the offset at runtime
      offset.emitPush(mv);
      new ConstValueGenerator(Type.INT_TYPE, sizeInBytes).emitPush(mv);
      mv.visitInsn(IDIV);
    }
  }
}
