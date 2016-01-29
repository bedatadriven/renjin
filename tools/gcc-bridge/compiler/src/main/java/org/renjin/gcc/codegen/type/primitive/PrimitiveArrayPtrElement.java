package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;


public class PrimitiveArrayPtrElement extends AbstractExprGenerator {
  
  private ExprGenerator arrayPointer;
  private ExprGenerator index;
  private GimplePrimitiveType componentType;

  public PrimitiveArrayPtrElement(ExprGenerator arrayPointer, ExprGenerator index) {
    this.arrayPointer = arrayPointer;
    this.index = index;
    
    GimpleArrayType arrayType = arrayPointer.getGimpleType().getBaseType();
    componentType = (GimplePrimitiveType) arrayType.getComponentType();
  }

  @Override
  public GimpleType getGimpleType() {
    return componentType;
  }

  @Override
  public void emitPrimitiveValue(MethodGenerator mv) {
    arrayPointer.emitPushPtrArrayAndOffset(mv);
    index.emitPrimitiveValue(mv);
    mv.visitInsn(Opcodes.IADD);
    mv.visitInsn(componentType.jvmType().getOpcode(Opcodes.IALOAD));
  }

  @Override
  public void emitStore(MethodGenerator mv, ExprGenerator valueGenerator) {
    arrayPointer.emitPushPtrArrayAndOffset(mv);
    index.emitPrimitiveValue(mv);
    mv.visitInsn(Opcodes.IADD);
    valueGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(componentType.jvmType().getOpcode(Opcodes.IASTORE));
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }
  
  private class AddressOf extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(componentType);
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
      arrayPointer.emitPushPtrArrayAndOffset(mv);
      index.emitPrimitiveValue(mv);
      mv.visitInsn(Opcodes.IADD);
    }

    @Override
    public ExprGenerator valueOf() {
      return PrimitiveArrayPtrElement.this;
    }
  }
}
