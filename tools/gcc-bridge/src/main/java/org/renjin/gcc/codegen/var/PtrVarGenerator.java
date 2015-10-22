package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.LValueGenerator;
import org.renjin.gcc.codegen.expr.PtrGenerator;
import org.renjin.gcc.gimple.type.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Generates loads and stores from a pointer variable
 */
public class PtrVarGenerator extends AbstractExprGenerator implements PtrGenerator, VarGenerator, LValueGenerator {

  private GimpleIndirectType type;

  /**
   * The local variable index storing the array backing the pointer
   */
  private int arrayVariableIndex;

  /**
   * The local varaible index storing the offset within the array
   */
  private int offsetVariableIndex;

  public PtrVarGenerator(GimpleType type, int arrayVariableIndex, int offsetVariableIndex) {
    this.type = (GimpleIndirectType) type;
    this.arrayVariableIndex = arrayVariableIndex;
    this.offsetVariableIndex = offsetVariableIndex;
  }

  public PtrVarGenerator(GimpleType type, LocalVarAllocator localVarAllocator) {
    this.type = (GimpleIndirectType) type;
    this.arrayVariableIndex = localVarAllocator.reserve(1);
    this.offsetVariableIndex = localVarAllocator.reserve(Type.INT_TYPE);
  }


  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    // NOOP
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    emitPushArray(mv);
    mv.visitVarInsn(ILOAD, offsetVariableIndex);
  }

  private void emitPushArray(MethodVisitor mv) {
    mv.visitVarInsn(ALOAD, arrayVariableIndex);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator ptrGenerator) {
    ptrGenerator.emitPushPtrArrayAndOffset(mv);

    mv.visitVarInsn(Opcodes.ISTORE, offsetVariableIndex);
    mv.visitVarInsn(Opcodes.ASTORE, arrayVariableIndex);
  }

  @Override
  public WrapperType getPointerType() {
    return WrapperType.forPointerType(this.type);
  }

  @Override
  public ExprGenerator valueOf() {
    if (type.getBaseType() instanceof GimpleArrayType) {
      GimpleArrayType arrayType = type.getBaseType();
      if (arrayType.getComponentType() instanceof GimplePrimitiveType) {
        return new PrimitiveArray();
      }
    } else if (type.getBaseType() instanceof GimplePrimitiveType) {
      return new PrimitiveValueType();
    } 

    throw new UnsupportedOperationException("baseType: " + type.getBaseType());
  }

  private class PrimitiveArray extends AbstractExprGenerator {

    @Override
    public GimpleArrayType getGimpleType() {
      return type.getBaseType();
    }

    @Override
    public ExprGenerator elementAt(ExprGenerator indexGenerator) {
      return new PrimitiveArrayElement(getGimpleType().getComponentType(), indexGenerator);
    }
  }

  private class PrimitiveArrayElement extends AbstractExprGenerator {

    private final GimplePrimitiveType componentType;
    private ExprGenerator indexGenerator;

    public PrimitiveArrayElement(GimpleType componentType, ExprGenerator indexGenerator) {
      this.componentType = (GimplePrimitiveType) componentType;
      this.indexGenerator = indexGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return componentType;
    }

    @Override
    public Type getValueType() {
      return componentType.jvmType();
    }

    @Override
    public void emitPushValue(MethodVisitor mv) {
      // IALOAD (array, offset) => (value)
      emitPushArray(mv);
      // compute index ( pointer offset + array index)
      pushComputeIndex(mv);

      // load
      mv.visitInsn(componentType.jvmType().getOpcode(IALOAD));
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      Preconditions.checkState(valueGenerator.getValueType().equals(componentType.jvmType()));

      // IALOAD (array, offset) => (value)
      emitPushArray(mv);
      pushComputeIndex(mv);
      valueGenerator.emitPushValue(mv);

      // store to array
      mv.visitInsn(componentType.jvmType().getOpcode(IASTORE));
    }

    private void pushComputeIndex(MethodVisitor mv) {
      // original pointer offset + array index
      mv.visitVarInsn(ILOAD, offsetVariableIndex);
      indexGenerator.emitPushValue(mv);
      mv.visitInsn(IADD);
    }

    @Override
    public ExprGenerator addressOf() {
      return new PrimitiveArrayElementPtr(this);
    }
  }

  private class PrimitiveArrayElementPtr extends AbstractExprGenerator {
    private PrimitiveArrayElement element;

    public PrimitiveArrayElementPtr(PrimitiveArrayElement element) {
      this.element = element;
    }

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(element.getGimpleType());
    }

    @Override
    public WrapperType getPointerType() {
      return WrapperType.of(element.getValueType());
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      emitPushArray(mv);
      element.pushComputeIndex(mv);
    }
  }

  private class PrimitiveValueType extends AbstractExprGenerator {

    @Override
    public GimplePrimitiveType getGimpleType() {
      return type.getBaseType();
    }

    @Override
    public ExprGenerator addressOf() {
      return PtrVarGenerator.this;
    }

    @Override
    public Type getValueType() {
      return getGimpleType().jvmType();
    }

    @Override
    public void emitPushValue(MethodVisitor mv) {
      // IALOAD : (array, offset) 
      emitPushArray(mv);
      mv.visitVarInsn(ILOAD, offsetVariableIndex);
      mv.visitInsn(getValueType().getOpcode(IALOAD));
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      Preconditions.checkState(valueGenerator.getValueType().equals(getValueType()));

      // IASTORE (array, offset, value)
      emitPushArray(mv);
      mv.visitVarInsn(ILOAD, offsetVariableIndex);
      valueGenerator.emitPushValue(mv);
      mv.visitInsn(getValueType().getOpcode(IASTORE));
    }
  }
}

