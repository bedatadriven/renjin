package org.renjin.gcc.translate.expr;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.RealJimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.type.ImIndirectType;
import org.renjin.gcc.translate.type.ImPrimitiveType;

public class ImWrappedPtrExpr extends AbstractImExpr implements ImIndirectExpr {

  private JimpleExpr expr; 
  private JimpleType wrapperType;
  private JimpleType arrayType;
  
  public ImWrappedPtrExpr(JimpleExpr expr, JimpleType type) {
    this.expr = expr;
    this.wrapperType = type;
    try {
      this.arrayType = new RealJimpleType(Class.forName(wrapperType.toString()).getField("array").getType());
    } catch (Exception e) {
      throw new RuntimeException(e); // should not happen
    }
  }

  @Override
  public ArrayRef translateToArrayRef(FunctionContext context) {
    return new ArrayRef(backingArray(), backingArrayIndex());
  }


  private JimpleExpr backingArray() {
    return new JimpleExpr(expr + ".<" + wrapperType + ": " + arrayType + " array>");
  }

  private JimpleExpr backingArrayIndex() {
    return new JimpleExpr(expr + ".<" + wrapperType + ": int offset>");
  }

  @Override
  public ImIndirectType type() {
    throw new UnsupportedOperationException();
  }
}
