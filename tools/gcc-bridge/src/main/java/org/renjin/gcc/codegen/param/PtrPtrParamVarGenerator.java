package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.LValueGenerator;
import org.renjin.gcc.codegen.expr.PtrGenerator;
import org.renjin.gcc.codegen.var.VarGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates code for a pointer to a pointer passed as a parameter.
 */
public class PtrPtrParamVarGenerator extends AbstractExprGenerator implements VarGenerator, PtrGenerator {
  
  private GimpleType gimpleType;
  private WrapperType pointerType;
  private int index;

  /**
   * 
   * @param gimpleType the {@code GimpleType} of the parameter
   * @param index the local variable index of the {@code Ptr} parameter
   */
  public PtrPtrParamVarGenerator(GimpleType gimpleType, WrapperType pointerType, int index) {
    this.gimpleType = gimpleType;
    this.pointerType = pointerType;
    this.index = index;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    // NOOP
  }

  @Override
  public GimpleType getGimpleType() {
    return gimpleType;
  }

  @Override
  public ExprGenerator valueOf() {
    return new Value();
  }


  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, index);
    pointerType.emitUnpackArrayAndOffset(mv);
  }

  private class Value extends AbstractExprGenerator implements LValueGenerator {

    @Override
    public GimpleType getGimpleType() {
      return gimpleType.getBaseType();
    }


    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      // DoublePtr x;
      // x.update(address, offset)
      mv.visitVarInsn(Opcodes.ALOAD, index);
      valueGenerator.emitPushPtrArrayAndOffset(mv);
      pointerType.emitInvokeUpdate(mv);
    }
  }
}
