package org.renjin.gcc.codegen.type.fun;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.repackaged.asm.Type;

public class FunPtrField extends SingleFieldStrategy {

  public FunPtrField(Type ownerClass, String fieldName) {
    super(ownerClass, fieldName, FunPtrStrategy.METHOD_HANDLE_TYPE);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType) {
    if(offset != 0) {
      throw new IllegalStateException("offset = " + offset);
    }
    return new FunPtr(Expressions.field(instance, FunPtrStrategy.METHOD_HANDLE_TYPE, fieldName));
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    memsetReference(mv, instance, byteValue, byteCount);
  }

}
