package org.renjin.gcc.translate.expr;

import org.renjin.gcc.translate.type.ImPointerType;
import org.renjin.gcc.translate.type.ImType;


public class ImPtrFieldExpr extends AbstractImExpr {
  
  private ImPointerType type;

  public ImPtrFieldExpr(ImPointerType type) {
    this.type = type;
  }

  @Override
  public ImType type() {
    return type;
  }
}
