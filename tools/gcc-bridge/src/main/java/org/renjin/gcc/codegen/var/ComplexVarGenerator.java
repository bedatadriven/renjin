package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleComplexType;

/**
 * Generates load/store for a variable of type {@code complex}
 * 
 * <p>We compile this as two double local variables.</p>
 */
public class ComplexVarGenerator extends AbstractExprGenerator implements VarGenerator {
  
  private GimpleComplexType type;
  private Type partType;
  private int realIndex;
  private int imaginaryIndex;

  public ComplexVarGenerator(GimpleComplexType type, int realIndex, int imaginaryIndex) {
    this.type = type;
    this.partType = type.getJvmPartType();
    this.realIndex = realIndex;
    this.imaginaryIndex = imaginaryIndex;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    
  }

  @Override
  public GimpleComplexType getGimpleType() {
    return type;
  }


  @Override
  public ExprGenerator realPart() {
    return new ValueVarGenerator(type.getPartType(), realIndex);
  }

  @Override
  public ExprGenerator imaginaryPart() {
    return new ValueVarGenerator(type.getPartType(), imaginaryIndex);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    // store real part
    valueGenerator.realPart().emitPrimitiveValue(mv);
    mv.visitVarInsn(partType.getOpcode(Opcodes.ISTORE), realIndex);
    
    // store imaginary part
    valueGenerator.imaginaryPart().emitPrimitiveValue(mv);
    mv.visitVarInsn(partType.getOpcode(Opcodes.ISTORE), imaginaryIndex);
  }

}
