package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;

/**
 * Emits bytecode for loading / storing array variables.
 * 
 */
public class ArrayVarGenerator extends AbstractExprGenerator implements VarGenerator {

  /**
   * The local variable index of the array
   */
  private final int arrayIndex;
  private Type componentType;
  private GimpleArrayType gimpleType;

  public ArrayVarGenerator(GimpleArrayType gimpleType, int arrayIndex) {
    this.gimpleType = gimpleType;
    this.arrayIndex = arrayIndex;

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
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {

    mv.visitLdcInsn(gimpleType.getUbound() - gimpleType.getLbound() + 1);

    if(componentType.equals(Type.DOUBLE_TYPE)) {
      mv.visitIntInsn(NEWARRAY, Opcodes.T_DOUBLE);
      
    } else if(componentType.equals(Type.INT_TYPE)) {
      mv.visitIntInsn(NEWARRAY, Opcodes.T_INT);

    } else if(componentType.equals(Type.BYTE_TYPE)) {
      mv.visitIntInsn(NEWARRAY, Opcodes.T_BYTE);
      
    } else {
      throw new UnsupportedOperationException("componentType: " + componentType);
    }
    mv.visitVarInsn(Opcodes.ASTORE, arrayIndex);
   
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    mv.visitVarInsn(ALOAD, arrayIndex);
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
      mv.visitVarInsn(ALOAD, arrayIndex);
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
    public void emitPrimitiveValue(MethodVisitor mv) {
      ArrayVarGenerator.this.emitPrimitiveValue(mv);
      indexGenerator.emitPrimitiveValue(mv);
      mv.visitInsn(componentType.getOpcode(IALOAD));    
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      ArrayVarGenerator.this.emitPrimitiveValue(mv);
      indexGenerator.emitPrimitiveValue(mv);
      valueGenerator.emitPrimitiveValue(mv);
      mv.visitInsn(componentType.getOpcode(IASTORE));
    }
  }
}
