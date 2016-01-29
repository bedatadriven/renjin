package org.renjin.gcc.codegen.type.primitive;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.ObjectPtr;


/**
 * Pointer to a pointer to a primitive, for example {@code double**}
 * 
 * <p>Given a Gimple local variable of type {@code double**}, we reserve local variable slots
 * for</p>
 * <pre>
 *   DoublePtr[] x;
 *   int x$offset = 0;
 * </pre>
 */
public class PrimitivePtrPtrVarGenerator extends AbstractExprGenerator implements VarGenerator {

  private final GimpleIndirectType pointerType;
  private final Var arrayVar;
  private final Var offsetVar;

  public PrimitivePtrPtrVarGenerator(GimpleIndirectType pointerType, Var arrayVar, Var offsetVar) {
    this.pointerType = pointerType;
    this.arrayVar = arrayVar;
    this.offsetVar = offsetVar;
  }

  @Override
  public void emitDefaultInit(MethodGenerator mv, Optional<ExprGenerator> initialValue) {
    if (initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public GimpleType getGimpleType() {
    return pointerType;
  }

  @Override
  public WrapperType getPointerType() {
    return WrapperType.valueOf(ObjectPtr.class);
  }

  @Override
  public void emitStore(MethodGenerator mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushPtrArrayAndOffset(mv);
    offsetVar.store(mv);
    arrayVar.store(mv);
  }

  @Override
  public void emitPushPointerWrapper(MethodGenerator mv) {
    super.emitPushPointerWrapper(mv);
  }

  @Override
  public void emitPushPtrArray(MethodGenerator mv) {
    arrayVar.load(mv);
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
    arrayVar.load(mv);
    offsetVar.load(mv);
  }

  @Override
  public ExprGenerator valueOf() {
    return new DereferencedPrimitivePtr(this);
  }


  @Override
  public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
    return new PrimitivePtrPtrPlus(this, offsetInBytes);
  }
}
