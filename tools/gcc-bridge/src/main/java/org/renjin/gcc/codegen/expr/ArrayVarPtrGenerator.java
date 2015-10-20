package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.var.ArrayVarGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

public class ArrayVarPtrGenerator extends AbstractExprGenerator implements PtrGenerator {
  
  private ArrayVarGenerator arrayVar;

  public ArrayVarPtrGenerator(ArrayVarGenerator arrayVar) {
    this.arrayVar = arrayVar;
  }

  @Override
  public void emitPushArrayAndOffset(MethodVisitor mv) {
    arrayVar.emitPushValue(mv);
    mv.visitInsn(Opcodes.ICONST_0);
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimplePointerType(arrayVar.getGimpleType());
  }

  @Override
  public WrapperType getPointerType() {
    return WrapperType.of(arrayVar.getComponentType());
  }
}
