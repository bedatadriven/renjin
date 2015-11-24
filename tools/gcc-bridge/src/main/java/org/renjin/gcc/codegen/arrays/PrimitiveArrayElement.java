package org.renjin.gcc.codegen.arrays;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.IASTORE;


public class PrimitiveArrayElement extends AbstractExprGenerator {
  private ExprGenerator arrayGenerator;
  private ExprGenerator indexGenerator;
  private GimpleArrayType arrayType;
  private Type componentType;

  public PrimitiveArrayElement(ExprGenerator arrayGenerator, ExprGenerator indexGenerator) {
    this.arrayGenerator = arrayGenerator;
    this.arrayType = (GimpleArrayType) arrayGenerator.getGimpleType();
    this.indexGenerator = indexGenerator;
    this.componentType = ((GimplePrimitiveType) arrayType.getComponentType()).jvmType();
  }

  @Override
  public GimpleType getGimpleType() {
    return arrayType.getComponentType();
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    arrayGenerator.emitPushArray(mv);
    indexGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(componentType.getOpcode(IALOAD));
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    arrayGenerator.emitPushArray(mv);
    indexGenerator.emitPrimitiveValue(mv);
    valueGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(componentType.getOpcode(IASTORE));
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }
  
  private class AddressOf extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(arrayType.getComponentType());
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      arrayGenerator.emitPushArray(mv);
      indexGenerator.emitPrimitiveValue(mv);
    }

    @Override
    public ExprGenerator valueOf() {
      return PrimitiveArrayElement.this;
    }
  }
}
