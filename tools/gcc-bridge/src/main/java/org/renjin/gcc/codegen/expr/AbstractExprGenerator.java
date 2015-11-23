package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.UnimplementedException;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

public abstract class AbstractExprGenerator implements ExprGenerator {

  @Override
  public ExprGenerator valueOf() {
    throw new UnimplementedException(getClass(), "valueOf");

  }

  @Override
  public ExprGenerator addressOf() {
    throw new UnimplementedException(getClass(), "addressOf");
  }

  @Override
  public ExprGenerator realPart() {
    throw new UnimplementedException(getClass(), "realPart");
  }

  @Override
  public ExprGenerator imaginaryPart() {
    throw new UnimplementedException(getClass(), "imaginaryPart");
  }

  @Override
  public ExprGenerator elementAt(ExprGenerator indexGenerator) {
    throw new UnimplementedException(getClass(), "elementAt");
  }

  @Override
  public final Type getJvmPrimitiveType() {
    if(getGimpleType() instanceof GimplePrimitiveType) {
      return ((GimplePrimitiveType) getGimpleType()).jvmType();
    } else {
      throw new UnsupportedOperationException(String.format("%s [%s] is not a value of primitive type (%s)",
          toString(), getClass().getSimpleName(), getGimpleType()));
    }
  }

  @Override
  public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
    throw new UnimplementedException(getClass(), "pointerPlus");
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    throw new UnimplementedException(getClass(), "emitPrimitiveValue");
  }
  
  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    throw new UnimplementedException(getClass(), "emitPushPtrArrayAndOffset");
  }

  @Override
  public void emitPushPtrArray(MethodVisitor mv) {
    emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(Opcodes.POP);
  }

  @Override
  public void emitPushArray(MethodVisitor mv) {
    throw new UnimplementedException(getClass(), "emitPushArray");
  }

  @Override
  public void emitPushMethodHandle(MethodVisitor mv) {
    throw new UnimplementedException(getClass(), "emitPushMethodHandle");

  }

  @Override
  public WrapperType getPointerType() {
    if(getGimpleType() instanceof GimpleIndirectType) {
      return WrapperType.forPointerType((GimpleIndirectType) getGimpleType());
    }
    throw new InternalCompilerException(getClass().getName() + " is not a pointer-typed expression.");
  }

  @Override
  public void emitPushPointerWrapper(MethodVisitor mv) {
    getPointerType().emitPushNewWrapper(mv, this);
  }

  @Override
  public void emitPushComplexAsArray(MethodVisitor mv) {
    Type partType = realPart().getJvmPrimitiveType();
    
    mv.visitInsn(Opcodes.ICONST_2);
    MallocGenerator.emitNewArray(mv, partType);
    mv.visitInsn(Opcodes.DUP);
    mv.visitInsn(Opcodes.ICONST_0);
    realPart().emitPrimitiveValue(mv);
    mv.visitInsn(partType.getOpcode(Opcodes.IASTORE));
    mv.visitInsn(Opcodes.DUP);
    mv.visitInsn(Opcodes.ICONST_1);
    imaginaryPart().emitPrimitiveValue(mv);
    mv.visitInsn(partType.getOpcode(Opcodes.IASTORE));
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    throw new UnimplementedException(getClass(), "emitStore");
  }

  @Override
  public boolean isConstantIntEqualTo(int value) {
    return false;
  }

  @Override
  public ExprGenerator memberOf(String memberName) {
    throw new UnimplementedException(getClass(), "memberOf");
  }

  @Override
  public void emitPushRecordRef(MethodVisitor mv) {
    throw new UnimplementedException(getClass(), "emitPushRecordRef");
  }

  @Override
  public ExprGenerator divideBy(int divisor) {
    return new PrimitiveBinOpGenerator(GimpleOp.EXACT_DIV_EXPR, this,
        new PrimitiveConstValueGenerator((GimpleIntegerType)getGimpleType(), divisor));
  }

  protected final void addOffsetInBytes(MethodVisitor mv, ExprGenerator offsetInBytes) {

    // convert bytes to elements by dividing by the element size in bytes
    ExprGenerator offsetCount = offsetToElements(offsetInBytes, getGimpleType().getBaseType().sizeOf());
    offsetCount.emitPrimitiveValue(mv);

    // add the offset in elements to the offset currently on the stack.
    mv.visitInsn(Opcodes.IADD);
  }

  /**
   * GCC often emits 
   * @param elementSize
   * @return
   */
  protected final ExprGenerator offsetToElements(ExprGenerator exprGenerator, int elementSize) {
    if(exprGenerator instanceof PrimitiveBinOpGenerator) {
      PrimitiveBinOpGenerator op = (PrimitiveBinOpGenerator) exprGenerator;
      if(op.getOp() == GimpleOp.MULT_EXPR) {
        // optimize for a common case where GCC will emit the offset as 
        // the product between i and the element size.
        // For example, if you have:
        //
        // double *x  
        // for(i=0;i<len;++i) sum += x[i]
        //
        // then GCC will emit x[i] as *(x + i*8)
        // To avoid the extra work of computing i*8/8, we should just use i

        if (op.getX().isConstantIntEqualTo(elementSize)) {
          return op.getY();
        } else if (op.getY().isConstantIntEqualTo(elementSize)) {
          return op.getX();
        }
      }
    } else if(exprGenerator instanceof PrimitiveConstValueGenerator) {
      // if the offset in bytes is a constant, then we can compute the value now
      PrimitiveConstValueGenerator constant = (PrimitiveConstValueGenerator) exprGenerator;
      return new PrimitiveConstValueGenerator(constant.getGimpleType(), 
          constant.getValue().intValue() / elementSize);
      
    } 
    // grr, need to compute at runtime
    return new PrimitiveBinOpGenerator(GimpleOp.EXACT_DIV_EXPR, exprGenerator, 
        new PrimitiveConstValueGenerator(new GimpleIntegerType(32), elementSize));
  }
  
  public void emitDebugging(MethodVisitor mv, String name, Label start, Label end) {
    
  }


}
