package org.renjin.gcc.codegen.type.primitive;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Emits bytecode for loading / storing array variables.
 * 
 */
public class PrimitiveArrayVar extends AbstractExprGenerator implements VarGenerator {

  /**
   * The local variable index of the array
   */
  private final Var arrayIndex;
  private final Type componentType;
  private final GimpleArrayType gimpleType;

  public PrimitiveArrayVar(GimpleArrayType gimpleType, Var arrayVar) {
    this.gimpleType = gimpleType;
    this.arrayIndex = arrayVar;

    GimpleType componentType = gimpleType.getComponentType();
    if(componentType instanceof GimplePrimitiveType) {
      this.componentType = ((GimplePrimitiveType) componentType).jvmType();
    } else if(componentType instanceof GimplePointerType) {
      this.componentType = WrapperType.wrapperType(componentType.getBaseType());
    } else {
      throw new UnsupportedOperationException("componentType: " + componentType);
    }
  }

  @Override
  public void emitDefaultInit(MethodGenerator mv, Optional<ExprGenerator> initialValue) {

    if(initialValue.isPresent()) {
      // provided an initial value for this array
      initialValue.get().emitPushArray(mv);
    } else {
      // allocate a new, empty array
      PrimitiveConstGenerator.emitInt(mv, gimpleType.getElementCount());
      MallocGenerator.emitNewArray(mv, componentType);

    }
    arrayIndex.store(mv);
  }

  @Override
  public void emitPushArray(MethodGenerator mv) {
    arrayIndex.load(mv);
  }

  @Override
  public void emitStore(MethodGenerator mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushArray(mv);
    arrayIndex.store(mv);
  }

  @Override
  public GimpleArrayType getGimpleType() {
    return gimpleType;
  }
  
  @Override
  public ExprGenerator addressOf() {
    return new AddressOfPrimitiveArray(this);
  }

  @Override
  public ExprGenerator elementAt(ExprGenerator indexGenerator) {
    return new PrimitiveArrayElement(this, indexGenerator);
  }

}
