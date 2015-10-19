package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.var.ArrayVarGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

public class ArrayVarPtrGenerator implements PtrGenerator {
  
  private ArrayVarGenerator arrayVar;

  public ArrayVarPtrGenerator(ArrayVarGenerator arrayVar) {
    this.arrayVar = arrayVar;
  }

  @Override
  public GimpleType gimpleBaseType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Type baseType() {
    return arrayVar.getComponentType();
  }

  @Override
  public void emitPushArrayAndOffset(MethodVisitor mv) {
    arrayVar.emitPush(mv);
    mv.visitInsn(Opcodes.ICONST_0);
  }
}
