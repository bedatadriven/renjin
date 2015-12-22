package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.Var;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.pointers.DereferencedPrimitiveValue;
import org.renjin.gcc.codegen.pointers.PrimitivePtrPlus;
import org.renjin.gcc.gimple.type.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Generates loads and stores from a pointer variable
 */
public class PrimitivePtrVarGenerator extends AbstractExprGenerator implements VarGenerator, ExprGenerator {

  private GimpleIndirectType type;

  /**
   * The local variable index storing the array backing the pointer
   */
  private Var arrayVar;

  /**
   * The local varaible index storing the offset within the array
   */
  private Var offsetVar;

  public PrimitivePtrVarGenerator(GimpleType type, Var arrayVar, Var offsetVar) {
    this.type = (GimpleIndirectType) type;
    this.arrayVar = arrayVar;
    this.offsetVar = offsetVar;
  }
  
  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public void emitPushPtrArray(MethodVisitor mv) {
    arrayVar.load(mv);
  }
  
  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    emitPushPtrArray(mv);
    offsetVar.load(mv);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator ptrGenerator) {
    ptrGenerator.emitPushPtrArrayAndOffset(mv);

    offsetVar.store(mv);
    arrayVar.store(mv);
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
        return new PrimitiveArray(arrayType);
      }
    } else if (type.getBaseType() instanceof GimplePrimitiveType) {
      return new DereferencedPrimitiveValue(this);
    } 

    throw new UnsupportedOperationException("baseType: " + type.getBaseType());
  }

  @Override
  public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
    return new PrimitivePtrPlus(this, offsetInBytes);
  }

  private class PrimitiveArray extends AbstractExprGenerator {
    private final GimpleArrayType arrayType;

    public PrimitiveArray(GimpleArrayType arrayType) {
      this.arrayType = arrayType;
    }

    @Override
    public GimpleArrayType getGimpleType() {
      return type.getBaseType();
    }

    @Override
    public ExprGenerator elementAt(ExprGenerator indexGenerator) {
      return new PrimitiveArrayElement(getGimpleType().getComponentType(), arrayType.getLbound(), indexGenerator);
    }
  }
  

  private class PrimitiveArrayElement extends AbstractExprGenerator {

    private final GimplePrimitiveType componentType;
    private ExprGenerator indexGenerator;
    private int lowerBound;

    public PrimitiveArrayElement(GimpleType componentType, int lowerBound, ExprGenerator indexGenerator) {
      this.lowerBound = lowerBound;
      this.componentType = (GimplePrimitiveType) componentType;
      this.indexGenerator = indexGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return componentType;
    }
    
    @Override
    public void emitPrimitiveValue(MethodVisitor mv) {
      // IALOAD (array, offset) => (value)
      PrimitivePtrVarGenerator.this.emitPushPtrArray(mv);
      // compute index ( pointer offset + array index)
      pushComputeIndex(mv);

      // load
      mv.visitInsn(componentType.jvmType().getOpcode(IALOAD));
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      Preconditions.checkState(valueGenerator.getJvmPrimitiveType().equals(componentType.jvmType()));

      // IALOAD (array, offset) => (value)
      PrimitivePtrVarGenerator.this.emitPushPtrArray(mv);
      pushComputeIndex(mv);
      valueGenerator.emitPrimitiveValue(mv);

      // store to array
      mv.visitInsn(componentType.jvmType().getOpcode(IASTORE));
    }

    private void pushComputeIndex(MethodVisitor mv) {
      // original pointer offset + array index
      offsetVar.load(mv);
      indexGenerator.emitPrimitiveValue(mv);
      mv.visitInsn(IADD);
      if(lowerBound != 0) {
        if(lowerBound != 1) {
          throw new UnsupportedOperationException("lbound: " + lowerBound);
        }
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ISUB);
      }
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
      return WrapperType.of(element.getJvmPrimitiveType());
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      PrimitivePtrVarGenerator.this.emitPushPtrArray(mv);
      element.pushComputeIndex(mv);
    }
  }

}

