package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates load/store instructions and more for a variable of type {@code complex *}
 * 
 * <p>We use two slots for a complex pointer: one local variables slot for a double[] array
 * reference, and one for the int offset.</p>
 */
public class ComplexPtrVarGenerator extends AbstractExprGenerator implements VarGenerator {
  
  private static final int REAL_OFFSET = 0;
  private static final int IM_OFFSET = 1;
  
  private GimpleType type;
  private int arrayIndex;
  private int offsetIndex;

  public ComplexPtrVarGenerator(GimpleType type, int arrayIndex, int offsetIndex) {
    this.type = type;
    this.arrayIndex = arrayIndex;
    this.offsetIndex = offsetIndex;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    
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
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
    mv.visitVarInsn(Opcodes.ILOAD, offsetIndex);
  }
  
  @Override
  public WrapperType getPointerType() {
    return WrapperType.of(Type.DOUBLE_TYPE);
  }
  
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
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      // Load our array onto the stack
      mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
      // Duplicate it so we can store to it twice
      // Stack: (array, array)
      mv.visitInsn(Opcodes.DUP);

      // Store the real part first
      mv.visitVarInsn(Opcodes.ILOAD, offsetIndex);
      valueGenerator.realPart().emitPushValue(mv);
      mv.visitInsn(Opcodes.DASTORE);

      // Now store the complex part
      mv.visitVarInsn(Opcodes.ILOAD, offsetIndex);
      mv.visitInsn(Opcodes.ICONST_1);
      mv.visitInsn(Opcodes.IADD);
      valueGenerator.imaginaryPart().emitPushValue(mv);
      mv.visitInsn(Opcodes.DASTORE);
    }

  }
  
  private class PartExpr extends AbstractExprGenerator {

    private final int offset;

    public PartExpr(int offset) {
      this.offset = offset;
    }

    @Override
    public GimpleType getGimpleType() {
      return new GimpleRealType(64);
    }

    @Override
    public Type getValueType() {
      return Type.DOUBLE_TYPE;
    }

    @Override
    public void emitPushValue(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
      mv.visitVarInsn(Opcodes.ILOAD, offsetIndex);
      if(offset == IM_OFFSET) {
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IADD);
      }
      mv.visitInsn(Opcodes.DALOAD);
    }
  }
}
