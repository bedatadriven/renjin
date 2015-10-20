package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.Types;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.IASTORE;

/**
 * Generates load/store operations from an array element reference
 */
public class ArrayRefGenerator extends AbstractExprGenerator implements ValueGenerator, LValueGenerator {
  
  private final ExprGenerator arrayGenerator;
  private final ValueGenerator indexGenerator;
  private final GimpleArrayType arrayType;

  public ArrayRefGenerator(ExprGenerator arrayGenerator, ExprGenerator indexGenerator) {
    this.arrayGenerator = arrayGenerator;
    this.indexGenerator = (ValueGenerator) indexGenerator;
    this.arrayType = (GimpleArrayType) arrayGenerator.getGimpleType();
  }

  @Override
  public GimpleType getGimpleType() {
    return arrayType.getComponentType();
  }

  @Override
  public Type getValueType() {
    return Types.getComponentType(arrayGenerator.getValueType());
  }

  @Override
  public void emitPushValue(MethodVisitor mv) {
    arrayGenerator.emitPushValue(mv);
    indexGenerator.emitPushValue(mv);
    mv.visitInsn(typeOpcode(IALOAD));
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {

    // ( array, index, value ) => ()
    
    arrayGenerator.emitPushValue(mv);
    indexGenerator.emitPushValue(mv);
    valueGenerator.emitPushValue(mv);

    mv.visitInsn(typeOpcode(IASTORE));
  }
  
  private int typeOpcode(int opcode) {
    GimpleType componentType = getGimpleType();
    Type type;
    if(componentType instanceof GimplePrimitiveType) {
      type = ((GimplePrimitiveType) componentType).jvmType();
    } else {
      type = Type.getType(Object.class);      
    }
    return type.getOpcode(opcode);
  }
}
