package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates load/store instructions from a variable storing a pointer to an array.
 */
public class ArrayPtrVarGenerator extends AbstractExprGenerator implements VarGenerator {
  
  private GimpleIndirectType gimpleType;
  private GimpleArrayType arrayType;
  private int arrayIndex;
  private int offsetIndex;

  public ArrayPtrVarGenerator(GimpleIndirectType gimpleType, int arrayIndex, int offsetIndex) {
    this.gimpleType = gimpleType;
    this.arrayType = gimpleType.getBaseType();
    this.arrayIndex = arrayIndex;
    this.offsetIndex = offsetIndex;

    Preconditions.checkArgument(arrayType.getLbound() == 0);
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    mv.visitInsn(Opcodes.ACONST_NULL);
    mv.visitVarInsn(Opcodes.ASTORE, arrayIndex);
    mv.visitInsn(Opcodes.ICONST_0);
    mv.visitVarInsn(Opcodes.ISTORE, offsetIndex);
  }

  @Override
  public GimpleType getGimpleType() {
    return gimpleType;
  }
  
  
  @Override
  public WrapperType getPointerType() {
    return WrapperType.forPointerType(gimpleType);
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
    mv.visitInsn(Opcodes.ICONST_0);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator ptrGenerator) {
    ptrGenerator.emitPushPtrArrayAndOffset(mv);

    mv.visitVarInsn(Opcodes.ISTORE, offsetIndex);
    mv.visitVarInsn(Opcodes.ASTORE, arrayIndex);
  }

  @Override
  public ExprGenerator valueOf() {
    return new ArrayValue();
  }

  private class ArrayValue extends AbstractExprGenerator {


    @Override
    public GimpleType getGimpleType() {
      return gimpleType.getBaseType();
    }

    @Override
    public ExprGenerator addressOf() {
      return ArrayPtrVarGenerator.this;
    }

    @Override
    public ExprGenerator elementAt(ExprGenerator indexGenerator) {
      return new ElementAt(indexGenerator);
    }
  }
  
  private class ElementAt extends AbstractExprGenerator {
    private final GimplePrimitiveType componentType;
    private ExprGenerator indexGenerator;

    public ElementAt(ExprGenerator indexGenerator) {
      this.indexGenerator = indexGenerator;
      GimpleArrayType arrayType = gimpleType.getBaseType();
      this.componentType = (GimplePrimitiveType) arrayType.getComponentType();
    }

    @Override
    public GimpleType getGimpleType() {
      return componentType;
    }
    
    private void emitPushIndex(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ILOAD, offsetIndex);
      indexGenerator.emitPrimitiveValue(mv);
      mv.visitInsn(Opcodes.IADD);
    }
    
    @Override
    public void emitPrimitiveValue(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
      emitPushIndex(mv);
      mv.visitInsn(componentType.jvmType().getOpcode(Opcodes.IALOAD));
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
      emitPushIndex(mv);
      valueGenerator.emitPrimitiveValue(mv);
      mv.visitInsn(componentType.jvmType().getOpcode(Opcodes.IASTORE));
    }
  }
}
