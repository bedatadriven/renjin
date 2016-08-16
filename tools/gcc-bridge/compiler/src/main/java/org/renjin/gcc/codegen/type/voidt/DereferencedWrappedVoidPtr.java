package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.fatptr.WrappedFatPtrExpr;
import org.renjin.repackaged.asm.Type;

public class DereferencedWrappedVoidPtr extends VoidPtr {

  private WrappedFatPtrExpr wrapperInstance;

  public DereferencedWrappedVoidPtr(WrappedFatPtrExpr wrapperInstance) {
    super(wrapperInstance.valueExpr(), wrapperInstance);
    this.wrapperInstance = wrapperInstance;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    if(rhs instanceof VoidPtr) {
      wrapperInstance.wrap().load(mv);
      ((VoidPtr) rhs).unwrap().load(mv);
      mv.invokevirtual(wrapperInstance.wrap().getType(), "set",
          Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), false);
    
    } else {
      throw new UnsupportedOperationException("TODO: rhs = " + rhs.getClass().getName());
    }
    
  }
}
