package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.Var;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.arrays.PrimitiveArrayPtrElement;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.NullPtrGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates load/store instructions from a variable storing a pointer to an array.
 */
public class PrimitiveArrayPtrVar extends AbstractExprGenerator implements VarGenerator {
  
  private GimpleIndirectType gimpleType;
  private GimpleArrayType arrayType;
  private Var arrayVar;
  private Var offsetVar;

  public PrimitiveArrayPtrVar(GimpleIndirectType gimpleType, Var arrayVar, Var offsetVar) {
    this.gimpleType = gimpleType;
    this.arrayType = gimpleType.getBaseType();
    this.arrayVar = arrayVar;
    this.offsetVar = offsetVar;

    Preconditions.checkArgument(arrayType.getLbound() == 0);
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    mv.visitInsn(Opcodes.ACONST_NULL);
    arrayVar.store(mv);
    mv.visitInsn(Opcodes.ICONST_0);
    offsetVar.store(mv);
    
    if(initialValue.isPresent() && !isDefaultValue(initialValue.get())) {
      emitStore(mv, initialValue.get());
    }
  }

  private boolean isDefaultValue(ExprGenerator exprGenerator) {
    return exprGenerator instanceof NullPtrGenerator;
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
    arrayVar.load(mv);
    offsetVar.load(mv);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator ptrGenerator) {
    ptrGenerator.emitPushPtrArrayAndOffset(mv);

    offsetVar.store(mv);
    arrayVar.store(mv);
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
      return PrimitiveArrayPtrVar.this;
    }

    @Override
    public ExprGenerator elementAt(ExprGenerator indexGenerator) {
      return new PrimitiveArrayPtrElement(addressOf(), indexGenerator);
    }
  }

}
