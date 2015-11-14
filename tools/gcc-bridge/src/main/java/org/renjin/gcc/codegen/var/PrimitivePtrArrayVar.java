package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * An array of pointers to primitives. Stored as an array of Ptrs, for example DoublePtr[]
 */
public class PrimitivePtrArrayVar extends AbstractExprGenerator implements VarGenerator {
  
  private GimpleArrayType arrayType;
  private int varIndex;
  private WrapperType wrapperType;

  public PrimitivePtrArrayVar(GimpleArrayType arrayType, int varIndex) {
    this.arrayType = arrayType;
    this.varIndex = varIndex;
    this.wrapperType = WrapperType.forPointerType((GimpleIndirectType) arrayType.getComponentType()); 
  }

  @Override
  public GimpleType getGimpleType() {
    return arrayType;
  }


  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    mv.visitInsn(Opcodes.ACONST_NULL);
    mv.visitVarInsn(Opcodes.ASTORE, varIndex);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushArray(mv);
    mv.visitVarInsn(Opcodes.ASTORE, varIndex);
  }

  @Override
  public ExprGenerator elementAt(ExprGenerator indexGenerator) {
    return new PtrElement(indexGenerator);  
  }


  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }

  private class PtrElement extends AbstractExprGenerator {
    private ExprGenerator indexGenerator;

    public PtrElement(ExprGenerator indexGenerator) {
      this.indexGenerator = indexGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return arrayType.getComponentType();
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      // Push the pointer in valueGenerator onto the stack as a DoublePtr
      // and store it to this index
      mv.visitVarInsn(Opcodes.ALOAD, varIndex);
      indexGenerator.emitPrimitiveValue(mv);
      valueGenerator.emitPushPointerWrapper(mv);
      mv.visitInsn(Opcodes.AASTORE);
    }


    @Override
    public void emitPushPointerWrapper(MethodVisitor mv) {
      // Push the pointer wrapper onto the stack
      mv.visitVarInsn(Opcodes.ALOAD, varIndex);
      indexGenerator.emitPrimitiveValue(mv);
      mv.visitInsn(Opcodes.AALOAD);  
    }
    
    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      emitPushPointerWrapper(mv);
      wrapperType.emitUnpackArrayAndOffset(mv);
    }
  }
  
  private class AddressOf extends AbstractExprGenerator {
    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(arrayType);
    }

    @Override
    public WrapperType getPointerType() {
      return WrapperType.OBJECT_PTR;
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      mv.visitVarInsn(Opcodes.ALOAD, varIndex);
      mv.visitInsn(Opcodes.ICONST_0);
    }
  }
}
