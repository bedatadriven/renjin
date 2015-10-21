package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;

/**
 * Emits bytecode for loading / storing array variables
 */
public class ArrayVarGenerator extends AbstractExprGenerator implements VarGenerator {

  /**
   * The local variable index of the array
   */
  private final int index;
  private Type componentType;
  private GimpleArrayType gimpleType;

  public ArrayVarGenerator(GimpleArrayType gimpleType, LocalVarAllocator localVarAllocator) {
    this.gimpleType = gimpleType;
    index = localVarAllocator.reserve(1);

    GimpleType componentType = gimpleType.getComponentType();
    if(componentType instanceof GimplePrimitiveType) {
      this.componentType = ((GimplePrimitiveType) componentType).jvmType();
    } else if(componentType instanceof GimplePointerType) {
      this.componentType = WrapperType.wrapperType(componentType.getBaseType());
    } else {
      throw new UnsupportedOperationException("componentType: " + this.componentType);
    }
  }

  @Override
  public Type getValueType() {
    return Type.getType("[" + componentType.getDescriptor());
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {

    mv.visitLdcInsn(gimpleType.getUbound() - gimpleType.getLbound() + 1);

    if(componentType.equals(Type.DOUBLE_TYPE)) {
      mv.visitIntInsn(NEWARRAY, Opcodes.T_DOUBLE);
    } else if(componentType.equals(Type.INT_TYPE)) {
      mv.visitIntInsn(NEWARRAY, Opcodes.T_INT);
    } else {
      throw new UnsupportedOperationException("componentType: " + componentType);
    }
    mv.visitVarInsn(Opcodes.ASTORE, index);
  }


  @Override
  public void emitPushValue(MethodVisitor mv) {
    mv.visitVarInsn(ALOAD, index);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GimpleArrayType getGimpleType() {
    return gimpleType;
  }
  
  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }

  @Override
  public ExprGenerator elementAt(ExprGenerator indexGenerator) {
    return new ElementAt(indexGenerator);
  }

  private class AddressOf extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(gimpleType);
    }

    @Override
    public WrapperType getPointerType() {
      return WrapperType.of(componentType);
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      mv.visitVarInsn(ALOAD, index);
      mv.visitInsn(ICONST_0);
    }

    @Override
    public ExprGenerator valueOf() {
      return ArrayVarGenerator.this;
    }
  }
  
  private class ElementAt extends AbstractExprGenerator {
    private ExprGenerator indexGenerator;

    public ElementAt(ExprGenerator indexGenerator) {
      this.indexGenerator = indexGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return gimpleType.getComponentType();
    }

    @Override
    public Type getValueType() {
      return componentType;
    }

    @Override
    public void emitPushValue(MethodVisitor mv) {
      ArrayVarGenerator.this.emitPushValue(mv);
      indexGenerator.emitPushValue(mv);
      mv.visitInsn(componentType.getOpcode(IALOAD));    
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      ArrayVarGenerator.this.emitPushValue(mv);
      indexGenerator.emitPushValue(mv);
      valueGenerator.emitPushValue(mv);
      mv.visitInsn(componentType.getOpcode(IASTORE));
    }
  }
}
