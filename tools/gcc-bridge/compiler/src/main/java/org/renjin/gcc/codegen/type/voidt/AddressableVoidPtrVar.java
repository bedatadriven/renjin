package org.renjin.gcc.codegen.type.voidt;

import com.google.common.base.Optional;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.var.Values;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.GimpleVoidType;


public class AddressableVoidPtrVar extends AbstractExprGenerator implements VarGenerator {
  
  private Var arrayIndex;

  public AddressableVoidPtrVar(Var arrayVar) {
    this.arrayIndex = arrayVar;
  }

  @Override
  public void emitDefaultInit(MethodGenerator mv, Optional<ExprGenerator> initialValue) {
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
    arrayIndex.store(mv, Values.newArray(Object.class, 1));
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimplePointerType(new GimpleVoidType());
  }

  @Override
  public void emitStore(MethodGenerator mv, ExprGenerator valueGenerator) {
    arrayIndex.load(mv);
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
    public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
      arrayIndex.load(mv);
      mv.visitInsn(Opcodes.ICONST_0);
    }

    @Override
    public void emitStore(MethodGenerator mv, ExprGenerator valueGenerator) {
      arrayIndex.load(mv);
      mv.visitInsn(Opcodes.ICONST_0);
      valueGenerator.emitPushPointerWrapper(mv);
      mv.visitInsn(Opcodes.AASTORE);
    }
  }
  
}
