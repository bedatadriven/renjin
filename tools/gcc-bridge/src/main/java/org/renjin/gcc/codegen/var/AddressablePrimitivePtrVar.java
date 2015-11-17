package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.NullPtrGenerator;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates a pointer variable whose address read and written to.
 * 
 * <p>For a pointer variable like {@code double *x}, for example, we compile this as
 * a local variable of type DoublePtr[] x = new DoublePtr[1]</p>
 *
 */
public class AddressablePrimitivePtrVar extends AbstractExprGenerator implements VarGenerator, ExprGenerator {
  
  private final GimpleIndirectType type;
  private final WrapperType wrapperType;
  private int index;

  public AddressablePrimitivePtrVar(GimpleType type, int localVarIndex) {
    this.index = localVarIndex;
    this.type = (GimpleIndirectType) type;
    this.wrapperType = WrapperType.forPointerType(this.type);
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public WrapperType getPointerType() {
    return wrapperType;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {

    // Create the unit array to hold the value
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitTypeInsn(Opcodes.ANEWARRAY, wrapperType.getWrapperType().getInternalName());
    mv.visitVarInsn(Opcodes.ASTORE, index);
    
    if(initialValue.isPresent() && !isDefaultValue(initialValue.get())) {
      emitStore(mv, initialValue.get());
    }
  }

  private boolean isDefaultValue(ExprGenerator exprGenerator) {
    return exprGenerator instanceof NullPtrGenerator;
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    // Push reference to DoublePtr[0].array and DoublePtr[0].offset
    // first push DoublePtr[0]
    mv.visitVarInsn(Opcodes.ALOAD, index);
    mv.visitInsn(Opcodes.ICONST_0);
    mv.visitInsn(Opcodes.AALOAD);
  
    // then unpack
    wrapperType.emitUnpackArrayAndOffset(mv);

  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    // Store reference to DoublePtr at array index 0
    
    mv.visitVarInsn(Opcodes.ALOAD, index);
    mv.visitInsn(Opcodes.ICONST_0);
    
    valueGenerator.emitPushPointerWrapper(mv);
  
    mv.visitInsn(Opcodes.AASTORE);
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }

  /**
   * The pointer's address,
   */
  private class AddressOf extends AbstractExprGenerator implements ExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(type);
    }

    @Override
    public WrapperType getPointerType() {
      return WrapperType.OBJECT_PTR;
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      // push Object[] and offset
      mv.visitVarInsn(Opcodes.ALOAD, index);
      mv.visitInsn(Opcodes.ICONST_0);
    }
  }
}
