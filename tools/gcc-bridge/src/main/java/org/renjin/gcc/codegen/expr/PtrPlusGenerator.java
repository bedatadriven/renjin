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
  private PrimitiveGenerator offset;

  public PtrPlusGenerator(ExprGenerator ptr, ExprGenerator offset) {
    this.ptr = (PtrGenerator) ptr;
    this.offset = (PrimitiveGenerator) offset;
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
  public boolean isSameArray(PtrGenerator other) {
    return ptr.isSameArray(other);
  }

  @Override
  public void emitPushArray(MethodVisitor mv) {
    ptr.emitPushArray(mv);
  }

  @Override
  public void emitPushOffset(MethodVisitor mv) {
    
    // Push the original offset onto the stack
    ptr.emitPushOffset(mv);
    
    // Push the offset change onto the stack:
    
    // Pointer arithmetic is done in bytes, we need to divide
    // by the size of the base type to arrive at the offset difference
    
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
    
    // Consume the original offset and the change and push the sum back onto the stack
    mv.visitInsn(IADD);
  }
}
