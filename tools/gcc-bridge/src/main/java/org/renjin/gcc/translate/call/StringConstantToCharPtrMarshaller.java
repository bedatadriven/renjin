package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.types.PrimitiveTypes;

public class StringConstantToCharPtrMarshaller extends ParamMarshaller {

  @Override
  public JimpleExpr marshall(FunctionContext context, GimpleExpr expr,
      CallParam param) {
    
    String value = ParamUtils.isStringConstant(expr);
    if(value != null && param instanceof WrappedPtrCallParam) {
      
      String temp = context.getBuilder().addTempVarDecl(PrimitiveTypes.getWrapperType(PrimitiveType.CHAR));
      StringBuilder stmt = new StringBuilder();
      stmt.append(temp).append(" = staticinvoke<org.renjin.gcc.runtime.CharPtr: org.renjin.gcc.runtime.CharPtr fromString(java.lang.String)>")
      .append("(").append(JimpleExpr.stringLiteral(value)).append(")");
      
      context.getBuilder().addStatement(stmt.toString());
      
      return new JimpleExpr(temp);
    }
    throw new CannotMarshallException();
  }

}
