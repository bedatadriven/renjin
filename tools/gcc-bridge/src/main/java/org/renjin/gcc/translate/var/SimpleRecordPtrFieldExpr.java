package org.renjin.gcc.translate.var;


import org.renjin.gcc.translate.expr.AbstractImExpr;
import org.renjin.gcc.translate.type.ImType;
import org.renjin.gcc.translate.type.struct.SimpleRecordPtrType;

public class SimpleRecordPtrFieldExpr extends AbstractImExpr {

  private SimpleRecordPtrType type;

  public SimpleRecordPtrFieldExpr(SimpleRecordPtrType type) {
    this.type = type;
  }

  @Override
  public ImType type() {
    return type;
  }
}
