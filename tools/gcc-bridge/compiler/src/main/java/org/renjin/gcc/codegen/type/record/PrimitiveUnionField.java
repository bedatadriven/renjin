package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.repackaged.asm.Type;

public class PrimitiveUnionField extends SingleFieldStrategy {
  
  private Type declaringClass;
  private Type fieldType;
  private String fieldName;

  public PrimitiveUnionField(Type declaringClass, String fieldName, Type fieldType) {
    super(declaringClass, fieldName, fieldType);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    throw new UnsupportedOperationException();
  }
}
