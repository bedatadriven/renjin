package org.renjin.gcc.translate.expr;

import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.expr.GimpleRealConstant;
import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.TypeChecker;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.PrimitiveType;
import org.renjin.gcc.translate.var.PrimitiveHeapVar;

/**
 * An expression that evaluations to a constant primitive value
 */
public class ImPrimitiveConstant extends AbstractImExpr {

  private FunctionContext context;
  private GimpleConstant constant;
  private ImPrimitiveType type;
  
  public ImPrimitiveConstant(FunctionContext context, GimpleConstant constant) {
    super();
    this.context = context;
    this.constant = constant;
    if(constant.getType() instanceof GimpleIndirectType) {
      // null basically, not sure if this right to handle this way
      this.type = ImPrimitiveType.INT;
    } else {
      this.type = ImPrimitiveType.valueOf(constant.getType());
    }
  }

  @Override
  public JimpleExpr translateToPrimitive(FunctionContext context, ImPrimitiveType type) {
    return type.literalExpr(constant.getValue());
  }

  @Override
  public ImPrimitiveType type() {
    return type;
  }

  
  public Object getConstantValue() {
    return constant.getValue();
  }
  
  @Override
  public String toString() {
    return constant.toString();
  }

  @Override
  public ImExpr addressOf() {
    // in order to provide an address, we'll create a heap variable on the fly
    PrimitiveHeapVar var = new PrimitiveHeapVar(context, type,
            "__constant" + System.identityHashCode(this));
    var.writePrimitiveAssignment(translateToPrimitive(context, type));

    return var.addressOf();
  }

  @Override
  public boolean isNull() {
    if(getConstantValue() instanceof Number) {
      Number value = (Number) getConstantValue();
      return value.intValue() == 0;
    }
    return false;
  }
}
