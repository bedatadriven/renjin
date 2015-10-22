package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleRealType;

/**
 * Generates load/store for a variable of type {@code complex}
 * 
 * <p>We compile this as two double local variables.</p>
 */
public class ComplexVarGenerator extends AbstractExprGenerator implements VarGenerator {
  
  private GimpleComplexType type;
  private int realIndex;
  private int imaginaryIndex;

  public ComplexVarGenerator(GimpleComplexType type, int realIndex, int imaginaryIndex) {
    Preconditions.checkArgument(type.sizeOf() == 128, "Expected only double precision complex value");
    this.type = type;
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
    return new ValueVarGenerator(new GimpleRealType(64), realIndex);
  }

  @Override
  public ExprGenerator imaginaryPart() {
    return new ValueVarGenerator(new GimpleRealType(64), imaginaryIndex);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    // store real part
    valueGenerator.realPart().emitPushValue(mv);
    mv.visitVarInsn(Opcodes.DSTORE, realIndex);
    
    // store imaginary part
    valueGenerator.imaginaryPart().emitPushValue(mv);
    mv.visitVarInsn(Opcodes.DSTORE, imaginaryIndex);
  }
}
