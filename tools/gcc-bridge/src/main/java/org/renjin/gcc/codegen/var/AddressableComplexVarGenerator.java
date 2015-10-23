package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates a local variable for a complex value type that is also addressable.
 * 
 * <p>This is accomplished by allocating an double[] array of length two to store 
 * the real and imaginary parts</p>
 */
public class AddressableComplexVarGenerator extends AbstractExprGenerator implements VarGenerator {
  
  private GimpleComplexType type;
  private Type partType;
  
  /**
   * Local variable index of the backing double[] array
   */
  private int arrayIndex;

  public AddressableComplexVarGenerator(GimpleComplexType type, int arrayIndex) {
    this.type = type;
    this.partType = type.getJvmPartType();
    this.arrayIndex = arrayIndex;
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }



  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
    mv.visitInsn(Opcodes.DUP);
    
    // stack (array, array)
    // store real part
    mv.visitInsn(Opcodes.ICONST_0);
    valueGenerator.realPart().emitPushValue(mv);
    mv.visitInsn(partType.getOpcode(Opcodes.IASTORE));
    
    // store imaginary part
    mv.visitInsn(Opcodes.ICONST_1);
    valueGenerator.imaginaryPart().emitPushValue(mv);
    mv.visitInsn(partType.getOpcode(Opcodes.IASTORE));
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    mv.visitInsn(Opcodes.ICONST_2);
    MallocGenerator.emitNewArray(mv, partType);
    mv.visitVarInsn(Opcodes.ASTORE, arrayIndex);  
  }

  @Override
  public ExprGenerator realPart() {
    return new PartValue(0);
  }
  
  @Override
  public ExprGenerator imaginaryPart() {
    return new PartValue(1);
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }

  private class PartValue extends AbstractExprGenerator {

    private int offset;

    public PartValue(int offset) {
      this.offset = offset;
    }

    @Override
    public GimpleType getGimpleType() {
      return type.getPartType();
    }

    @Override
    public Type getValueType() {
      return partType;
    }

    @Override
    public void emitPushValue(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
      mv.visitInsn(Opcodes.ICONST_0 + offset);
      mv.visitInsn(partType.getOpcode(Opcodes.IALOAD));
    }
  }
  
  private class AddressOf extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(type);
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
      mv.visitInsn(Opcodes.ICONST_0);
    }
  }
  
  
}
