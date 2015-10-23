package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PtrGenerator;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates a pointer variable whose address read and written to.
 *
 */
public class AddressablePtrVarGenerator extends AbstractExprGenerator implements PtrGenerator, VarGenerator {
  
  private final GimpleIndirectType type;
  private final WrapperType wrapperType;
  private int index;

  public AddressablePtrVarGenerator(GimpleType type, int localVarIndex) {
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
  public void emitDefaultInit(MethodVisitor mv) {
    // initialize with a null pointer
    // DoublePtr x = new DoublePtr(null, 0);
    mv.visitTypeInsn(Opcodes.NEW, wrapperType.getWrapperType().getInternalName());
    mv.visitInsn(Opcodes.DUP);
    
    mv.visitInsn(Opcodes.ACONST_NULL);
    mv.visitInsn(Opcodes.ICONST_0);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, wrapperType.getWrapperType().getInternalName(), "<init>",
        wrapperType.getConstructorDescriptor(), false);
    
    mv.visitVarInsn(Opcodes.ASTORE, index);
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, index);
    wrapperType.emitUnpackArrayAndOffset(mv);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    
    // call x.update(array, offset) to change this pointer's address
    mv.visitVarInsn(Opcodes.ALOAD, index);
    
    PtrGenerator ptr = (PtrGenerator) valueGenerator;
    ptr.emitPushPtrArrayAndOffset(mv);
  
    wrapperType.emitInvokeUpdate(mv);
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }

  private class AddressOf extends AbstractExprGenerator implements PtrGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(type);
    }

    @Override
    public WrapperType getPointerType() {
      return wrapperType;
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void emitPushPointerWrapper(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, index);
    }
  }
}
