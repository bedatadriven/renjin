package org.renjin.gcc.codegen.type.record.fat;

import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

public class AddressOfRecordArray extends AbstractExprGenerator {
  
  private ExprGenerator array;

  public AddressOfRecordArray(ExprGenerator array) {
    this.array = array;
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimplePointerType(array.getGimpleType());
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
    array.emitPushArray(mv);
    mv.visitInsn(Opcodes.ICONST_0);
  }

  @Override
  public ExprGenerator valueOf() {
    return array;
  }
}
