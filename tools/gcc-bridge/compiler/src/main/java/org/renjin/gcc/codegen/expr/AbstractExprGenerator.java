package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.UnimplementedException;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveConstGenerator;
import org.renjin.gcc.codegen.type.primitive.op.PrimitiveBinOpGenerator;
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
  public void emitPrimitiveValue(MethodGenerator mv) {
    throw new UnimplementedException(getClass(), "emitPrimitiveValue");
  }

  @Override
  public final void emitPushBoxedPrimitiveValue(MethodGenerator mv) {
    emitPrimitiveValue(mv);
    
    Type type = ((GimplePrimitiveType) getGimpleType()).jvmType();
    if(type.equals(Type.INT_TYPE)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Integer.class),
          "valueOf", "(I)Ljava/lang/Integer;", false);
    
    } else if(type.equals(Type.DOUBLE_TYPE)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Double.class),
          "valueOf", "(D)Ljava/lang/Double;", false);

    } else if(type.equals(Type.FLOAT_TYPE)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Float.class),
          "valueOf", "(F)Ljava/lang/Float;", false);

    } else if (type.equals(Type.BYTE_TYPE)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Byte.class),
          "valueOf", "(B)Ljava/lang/Byte", false);
    
    } else {
      throw new UnsupportedOperationException("type: " + type);
    }
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
    throw new UnimplementedException(getClass(), "emitPushPtrArrayAndOffset");
  }

  @Override
  public void emitPushPtrArray(MethodGenerator mv) {
    emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(Opcodes.POP);
  }

  @Override
  public void emitPushPtrRefForNullComparison(MethodGenerator mv) {
    emitPushPtrArray(mv);
  }
  
  @Override
  public void emitPushArray(MethodGenerator mv) {
    throw new UnimplementedException(getClass(), "emitPushArray");
  }

  @Override
  public void emitPushMethodHandle(MethodGenerator mv) {
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
  public void emitPushPointerWrapper(MethodGenerator mv) {
    getPointerType().emitPushNewWrapper(mv, this);
  }

  @Override
  public void emitPushComplexAsArray(MethodGenerator mv) {
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
  public void emitStore(MethodGenerator mv, ExprGenerator valueGenerator) {
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
  public void emitPushRecordRef(MethodGenerator mv) {
    throw new UnimplementedException(getClass(), "emitPushRecordRef");
  }

  protected final void addOffsetInBytes(MethodGenerator mv, ExprGenerator offsetInBytes) {

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
    } else if(exprGenerator instanceof PrimitiveConstGenerator) {
      // if the offset in bytes is a constant, then we can compute the value now
      PrimitiveConstGenerator constant = (PrimitiveConstGenerator) exprGenerator;
      return new PrimitiveConstGenerator(constant.getGimpleType(), 
          constant.getValue().intValue() / elementSize);
      
    } 
    // grr, need to compute at runtime
    return new PrimitiveBinOpGenerator(GimpleOp.EXACT_DIV_EXPR, exprGenerator, 
        new PrimitiveConstGenerator(new GimpleIntegerType(32), elementSize));
  }


}
