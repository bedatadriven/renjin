package org.renjin.gcc.codegen.pointers;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;


public class AddressOfPrimitiveValue extends AbstractExprGenerator {

  private ExprGenerator valueGenerator;

  public AddressOfPrimitiveValue(ExprGenerator valueGenerator) {
    this.valueGenerator = valueGenerator;
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimplePointerType(valueGenerator.getGimpleType());
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    Type type = valueGenerator.getJvmPrimitiveType();

    // Allocate a new array of size 1 and push to the stack
    mv.visitInsn(ICONST_1);
    MallocGenerator.emitNewArray(mv, type);

    // Initialize first and only element
    // IASTORE: (array, index, value) 
    mv.visitInsn(DUP);
    mv.visitInsn(ICONST_0);
    valueGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(type.getOpcode(IASTORE));

    // should still have the array on the stack

    // now push the offset
    mv.visitInsn(ICONST_0);
  }
}
