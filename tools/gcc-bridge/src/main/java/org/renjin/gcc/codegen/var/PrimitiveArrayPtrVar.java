package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.arrays.PrimitiveArrayElement;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.NullPtrGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates load/store instructions from a variable storing a pointer to an array.
 */
public class PrimitiveArrayPtrVar extends AbstractExprGenerator implements VarGenerator {
  
  private GimpleIndirectType gimpleType;
  private GimpleArrayType arrayType;
  private int arrayIndex;
  private int offsetIndex;

  public PrimitiveArrayPtrVar(GimpleIndirectType gimpleType, int arrayIndex, int offsetIndex) {
    this.gimpleType = gimpleType;
    this.arrayType = gimpleType.getBaseType();
    this.arrayIndex = arrayIndex;
    this.offsetIndex = offsetIndex;

    Preconditions.checkArgument(arrayType.getLbound() == 0);
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    mv.visitInsn(Opcodes.ACONST_NULL);
    mv.visitVarInsn(Opcodes.ASTORE, arrayIndex);
    mv.visitInsn(Opcodes.ICONST_0);
    mv.visitVarInsn(Opcodes.ISTORE, offsetIndex);
    
    if(initialValue.isPresent() && !isDefaultValue(initialValue.get())) {
      emitStore(mv, initialValue.get());
    }
  }

  private boolean isDefaultValue(ExprGenerator exprGenerator) {
    return exprGenerator instanceof NullPtrGenerator;
  }

  @Override
  public GimpleType getGimpleType() {
    return gimpleType;
  }
  
  
  @Override
  public WrapperType getPointerType() {
    return WrapperType.forPointerType(gimpleType);
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
    mv.visitVarInsn(Opcodes.ILOAD, offsetIndex);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator ptrGenerator) {
    ptrGenerator.emitPushPtrArrayAndOffset(mv);

    mv.visitVarInsn(Opcodes.ISTORE, offsetIndex);
    mv.visitVarInsn(Opcodes.ASTORE, arrayIndex);
  }

  @Override
  public ExprGenerator valueOf() {
    return new ArrayValue();
  }

  private class ArrayValue extends AbstractExprGenerator {


    @Override
    public GimpleType getGimpleType() {
      return gimpleType.getBaseType();
    }

    @Override
    public ExprGenerator addressOf() {
      return PrimitiveArrayPtrVar.this;
    }

    @Override
    public ExprGenerator elementAt(ExprGenerator indexGenerator) {
      return new PrimitiveArrayElement(this, indexGenerator);
    }
  }

}
