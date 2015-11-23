package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.GimpleVoidType;


public class AddressableVoidPtrVar extends AbstractExprGenerator implements VarGenerator {
  
  private int arrayIndex;

  public AddressableVoidPtrVar(int arrayIndex) {
    this.arrayIndex = arrayIndex;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
    mv.visitVarInsn(Opcodes.ASTORE, arrayIndex);
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimplePointerType(new GimpleVoidType());
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
    mv.visitInsn(Opcodes.ICONST_0);
    valueGenerator.emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(Opcodes.AASTORE);
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }
  
  private class AddressOf extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(new GimplePointerType(new GimpleVoidType()));
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
      mv.visitInsn(Opcodes.ICONST_0);
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
      mv.visitInsn(Opcodes.ICONST_0);
      valueGenerator.emitPushPointerWrapper(mv);
      mv.visitInsn(Opcodes.AASTORE);
    }
  }
  
}
