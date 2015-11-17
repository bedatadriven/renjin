package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IDIV;

/**
 * Generates code for pointer addition
 */
public class PtrPlusGenerator extends AbstractExprGenerator implements ExprGenerator {
  
  private ExprGenerator ptr;
  private ExprGenerator offset;

  public PtrPlusGenerator(ExprGenerator ptr, ExprGenerator offset) {
    this.ptr = ptr;
    this.offset = offset;
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    ptr.emitPushPtrArrayAndOffset(mv);

    // Now add the delta to the offset on the stack and add
    // it to the current offset
    pushDelta(mv);
    mv.visitInsn(IADD);
  }

  private void pushDelta(MethodVisitor mv) {
    int sizeInBytes = ptr.getGimpleType().getBaseType().sizeOf();

    if(offset instanceof PrimitiveConstValueGenerator) {
      // If the pointer is incremented by a constant amount, we can calculate the offset now
      ((PrimitiveConstValueGenerator) offset).divideBy(sizeInBytes).emitPrimitiveValue(mv);
      
    } else {
      // Otherwise we have to compute the offset at runtime
      offset.emitPrimitiveValue(mv);
      new PrimitiveConstValueGenerator(new GimpleIntegerType(32), sizeInBytes).emitPrimitiveValue(mv);
      mv.visitInsn(IDIV);
    }
  }

  @Override
  public ExprGenerator valueOf() {
    return new ValueOf();
  }

  @Override
  public GimpleType getGimpleType() {
    return ptr.getGimpleType();
  }
  
  private class ValueOf extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return ptr.getGimpleType().getBaseType();
    }

    @Override
    public void emitPrimitiveValue(MethodVisitor mv) {
      // push the array + offset onto the stack
      ptr.emitPushPtrArrayAndOffset(mv);
      
      // now consume the original offset and add the difference.
      pushDelta(mv);
      mv.visitInsn(Opcodes.IADD);

      // push the raw
      
    }
  }
  
}
