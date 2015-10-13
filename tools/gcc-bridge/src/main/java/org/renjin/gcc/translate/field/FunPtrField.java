package org.renjin.gcc.translate.field;

import org.renjin.gcc.translate.expr.AbstractImExpr;
import org.renjin.gcc.translate.type.ImFunctionType;
import org.renjin.gcc.translate.type.ImType;

/**
 * Function Pointer Field expression
 */
public class FunPtrField extends AbstractImExpr {
  private ImFunctionType baseType;
  private String memberName;

  public FunPtrField(ImFunctionType baseType, String memberName) {
    this.baseType = baseType;
    this.memberName = memberName;
  }

  @Override
  public ImType type() {
    return baseType;
  }
}
