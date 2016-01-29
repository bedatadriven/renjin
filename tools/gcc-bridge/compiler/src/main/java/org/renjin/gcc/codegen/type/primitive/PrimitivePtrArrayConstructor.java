package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

public class PrimitivePtrArrayConstructor extends AbstractExprGenerator {
  
  private GimpleArrayType arrayType;
  private GimplePrimitiveType primitiveType;
  private List<ExprGenerator> elements;
  private WrapperType componentType;

  public PrimitivePtrArrayConstructor(GimpleArrayType arrayType, List<ExprGenerator> elements) {
    this.arrayType = arrayType;
    this.primitiveType = arrayType.getComponentType().getBaseType();
    this.elements = elements;
    this.componentType = WrapperType.of(primitiveType);
  }

  @Override
  public GimpleType getGimpleType() {
    return arrayType;
  }

  @Override
  public void emitPushArray(MethodGenerator mv) {
    PrimitiveConstGenerator.emitInt(mv, arrayType.getElementCount());
    mv.visitTypeInsn(Opcodes.ANEWARRAY, componentType.getWrapperType().getInternalName());

    for (int i = 0; i < elements.size(); i++) {
      // keep the array on the stack
      mv.visitInsn(Opcodes.DUP);
      
      // index + value
      PrimitiveConstGenerator.emitInt(mv, i);
      elements.get(i).emitPushPointerWrapper(mv);
      
      // store to array
      mv.visitInsn(Opcodes.AASTORE);
    }
  }
}
