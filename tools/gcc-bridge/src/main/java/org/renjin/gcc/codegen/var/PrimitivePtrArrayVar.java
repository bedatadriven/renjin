package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.Var;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.pointers.PrimitivePtrPlus;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * An array of pointers to primitives, such as {@code double* x[]} 
 * 
 * <p>Arrays in gimple have a fixed size, and are allocated on the stack.</p>
 *
 * <p>We compile this to a local array variable of type {@code DoublePtr[]}, allocated at the start of the method 
 * on the heap. 
 */
public class PrimitivePtrArrayVar extends AbstractExprGenerator implements VarGenerator {
  
  private GimpleArrayType arrayType;
  private Var arrayVar;
  private WrapperType wrapperType;

  public PrimitivePtrArrayVar(GimpleArrayType arrayType, Var arrayVar) {
    this.arrayType = arrayType;
    this.arrayVar = arrayVar;
    this.wrapperType = WrapperType.forPointerType((GimpleIndirectType) arrayType.getComponentType()); 
  }

  @Override
  public GimpleType getGimpleType() {
    return arrayType;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {

    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    } else {
      mv.visitInsn(arrayType.getElementCount());
      mv.visitTypeInsn(Opcodes.ANEWARRAY, wrapperType.getWrapperType().getInternalName());
      arrayVar.store(mv);
    }
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushArray(mv);
    arrayVar.store(mv);
  }

  @Override
  public ExprGenerator elementAt(ExprGenerator indexGenerator) {
    return new PtrElement(indexGenerator);  
  }


  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }

  /**
   * Generates the address of this array, {@code &x}, which would be of type {@code double**}
   */
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
      arrayVar.load(mv);
      mv.visitInsn(Opcodes.ICONST_0);
    }
  }

  /**
   * Generates the pointer found at a given index within this array of pointers. 
   * 
   * <p>This expression's type is the type of the array's component. So if we are variable
   * {@code double * x[]}, then this is {@code x[i]}, of type {@code double*}
   */
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
    public WrapperType getPointerType() {
      return wrapperType;
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      // Push the pointer in valueGenerator onto the stack as a DoublePtr
      // and store it to this index
      arrayVar.load(mv);
      indexGenerator.emitPrimitiveValue(mv);
      valueGenerator.emitPushPointerWrapper(mv);
      mv.visitInsn(Opcodes.AASTORE);
    }


    @Override
    public void emitPushPointerWrapper(MethodVisitor mv) {
      // Push the pointer wrapper onto the stack
      arrayVar.load(mv);
      indexGenerator.emitPrimitiveValue(mv);
      mv.visitInsn(Opcodes.AALOAD);  
    }
    
    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      emitPushPointerWrapper(mv);
      wrapperType.emitUnpackArrayAndOffset(mv);
    }

    @Override
    public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
      return new PrimitivePtrPlus(this, offsetInBytes);
    }
  }
}
