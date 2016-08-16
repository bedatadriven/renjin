package org.renjin.gcc.codegen.type.record.unit;

import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.repackaged.asm.Type;


public class RecordUnitPtrField extends SingleFieldStrategy {

  public RecordUnitPtrField(Type ownerClass, String fieldName, Type fieldType) {
    super(ownerClass, fieldName, fieldType);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType) {
    if(offset != 0) {
      throw new IllegalStateException("offset = " + offset);
    }
    return new RecordUnitPtr(Expressions.field(instance, fieldType, fieldName));
  }
}
