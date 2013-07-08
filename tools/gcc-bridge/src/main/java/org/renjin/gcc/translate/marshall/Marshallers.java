package org.renjin.gcc.translate.marshall;


import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.call.CallParam;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImLValue;
import org.renjin.gcc.translate.expr.JvmExprs;

import java.util.List;

public class Marshallers {
  
  public static Marshaller forType(JimpleType type) {
    if(type.isPrimitive()) {
      return new PrimitiveMarshaller(type);
    } else if(type.isPointerWrapper()) {
      return new PointerWrapperMarshaller();
    } else if(type.isFunctionPointer()) {
      return new FunPtrMarshaller();
    } else {
      return new ObjectMarshaller(type);
    }
  }

  public static JimpleExpr marshallReturnValue(FunctionContext context, ImExpr returnValue) {
    JimpleType returnType = context.getBuilder().getReturnType();
    return marshall(context, returnValue, returnType);
  }
  
  public static void writeCall(FunctionContext context, GimpleCall call, String callExpr, JimpleType returnType) {
    if(call.getLhs() == null) {
      context.getBuilder().addStatement(callExpr.toString());
    } else {
      ImLValue lvalue = (ImLValue) context.resolveExpr(call.getLhs());
      ImExpr rhs = JvmExprs.toExpr(context, new JimpleExpr(callExpr), returnType, false);
      lvalue.writeAssignment(context, rhs);
    }
  }

  public static JimpleExpr marshall(FunctionContext context, ImExpr returnValue, JimpleType type) {
    return forType(type).marshall(context, returnValue);
  }


  public static String marshallParamList(FunctionContext context, GimpleCall call, List<CallParam> params) {
   StringBuilder paramList = new StringBuilder();
   paramList.append("(");
    boolean needsComma = false;
    for (JimpleExpr param : marshallParams(context, call, params)) {
      if (needsComma) {
        paramList.append(", ");
      }
      paramList.append(param.toString());
      needsComma = true;
    }
    paramList.append(")");
    return paramList.toString();
  }

  private static List<JimpleExpr> marshallParams(FunctionContext context,
                                                 GimpleCall call,
                                                 List<CallParam> callParams) {
    List<JimpleExpr> expressions = Lists.newArrayList();
    for (int i = 0; i != call.getParamCount(); ++i) {
      ImExpr sourceExpr = context.resolveExpr(call.getArguments().get(i));
      expressions.add(callParams.get(i).marshall(context, sourceExpr));
    }
    return expressions;
  }
  
}
