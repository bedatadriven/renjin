package org.renjin.gcc.codegen.type.complex;

import com.google.common.base.Optional;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.NullPtrGenerator;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.var.Values;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates load/store instructions and more for a variable of type {@code complex *}
 * 
 * <p>We use two slots for a complex pointer: one local variables slot for a double[] array
 * reference, and one for the int offset.</p>
 * 
 * <p>Note that the offset is the absolute index within the underlying double[] array: that is,
 * if the offset is 4, that means the real part is at {@code array[4]}, not {@code array[4*2]}.</p>
 */
public class ComplexPtrVarGenerator extends AbstractExprGenerator implements VarGenerator {
  
  private static final int REAL_OFFSET = 0;
  private static final int IM_OFFSET = 1;
  
  private GimpleIndirectType type;
  private GimpleComplexType baseType;
  private Type partType;
  private Var arrayVar;
  private Var offsetVar;

  public ComplexPtrVarGenerator(GimpleType type, Var arrayVar, Var offsetVar) {
    this.type = (GimpleIndirectType) type;
    this.baseType = type.getBaseType();
    this.partType = baseType.getJvmPartType();
    this.arrayVar = arrayVar;
    this.offsetVar = offsetVar;
  }

  @Override
  public void emitDefaultInit(MethodGenerator mv, Optional<ExprGenerator> initialValue) {
    arrayVar.store(mv, Values.nullRef());
    offsetVar.store(mv, Values.zero());
    
    if(initialValue.isPresent() && !isDefaultValue(initialValue.get())) {
      emitStore(mv, initialValue.get());
    }
  }

  private boolean isDefaultValue(ExprGenerator exprGenerator) {
    return exprGenerator instanceof NullPtrGenerator;
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public ExprGenerator valueOf() {
    return new Value();
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
    arrayVar.load(mv);
    offsetVar.load(mv);
  }

  @Override
  public void emitStore(MethodGenerator mv, ExprGenerator valueGenerator) {
    arrayVar.store(mv, valueGenerator.getPointerArray());
    offsetVar.store(mv, valueGenerator.getPointerOffset());
  }
  
  @Override
  public WrapperType getPointerType() {
    return WrapperType.of(partType);
  }

  /**
   * Generates loads and stores to the array value pointed to by the offset value
   */
  private class Value extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return type.getBaseType();
    }

    @Override
    public ExprGenerator realPart() {
      return new PartExpr(REAL_OFFSET);
    }

    @Override
    public ExprGenerator imaginaryPart() {
      return new PartExpr(IM_OFFSET);
    }

    @Override
    public void emitStore(MethodGenerator mv, ExprGenerator valueGenerator) {
      realPart().emitStore(mv, valueGenerator.realPart());
      imaginaryPart().emitStore(mv, valueGenerator.imaginaryPart());
    }
  }
  
  private class PartExpr extends AbstractExprGenerator {

    private final int offset;

    public PartExpr(int offset) {
      this.offset = offset;
    }

    @Override
    public GimpleType getGimpleType() {
      return baseType.getPartType();
    }
    
    private void pushIndex(MethodGenerator mv) {
      offsetVar.load(mv);
      if(offset == IM_OFFSET) {
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IADD);
      }
    }

    @Override
    public void emitPrimitiveValue(MethodGenerator mv) {
      arrayVar.load(mv);
      pushIndex(mv);
      mv.visitInsn(partType.getOpcode(Opcodes.IALOAD));
    }
  }

}
