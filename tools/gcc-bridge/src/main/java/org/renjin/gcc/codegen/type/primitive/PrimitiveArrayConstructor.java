package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

public class PrimitiveArrayConstructor extends AbstractExprGenerator {
  
  private GimpleArrayType arrayType;
  private GimplePrimitiveType componentType;
  private List<ExprGenerator> elements;

  public PrimitiveArrayConstructor(GimpleArrayType arrayType, List<ExprGenerator> elements) {
    this.arrayType = arrayType;
    this.componentType = (GimplePrimitiveType) arrayType.getComponentType();
    this.elements = elements;
  }

  @Override
  public GimpleType getGimpleType() {
    return arrayType;
  }

  @Override
  public void emitPushArray(MethodVisitor mv) {
    PrimitiveConstGenerator.emitInt(mv, arrayType.getElementCount());
    MallocGenerator.emitNewArray(mv, componentType.jvmType());

    for (int i = 0; i < elements.size(); i++) {
      // keep the array on the stack
      mv.visitInsn(Opcodes.DUP);

      // index + value
      PrimitiveConstGenerator.emitInt(mv, i);
      elements.get(i).emitPrimitiveValue(mv);

      // store to array
      mv.visitInsn(componentType.jvmType().getOpcode(Opcodes.IASTORE));
    }
  }
}
