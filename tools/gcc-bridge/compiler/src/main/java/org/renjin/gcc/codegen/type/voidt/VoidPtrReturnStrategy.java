package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.PointerTypeStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.repackaged.asm.Type;

/**
 * Strategy for returning and receiving void pointers.
 */
public class VoidPtrReturnStrategy implements ReturnStrategy {
  @Override
  public Type getType() {
    return Type.getType(Object.class);
  }

  @Override
  public JExpr marshall(GExpr expr) {
    return ((VoidPtr) expr).unwrap();
  }

  @Override
  public GExpr unmarshall(MethodGenerator mv, JExpr returnValue, TypeStrategy lhsTypeStrategy) {
    return ((PointerTypeStrategy) lhsTypeStrategy).unmarshallVoidPtrReturnValue(mv, returnValue);
  }

  @Override
  public JExpr getDefaultReturnValue() {
    return Expressions.nullRef(Type.getType(Object.class));
  }
}
