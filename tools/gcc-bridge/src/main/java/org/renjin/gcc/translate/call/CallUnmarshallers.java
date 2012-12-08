package org.renjin.gcc.translate.call;

import java.util.List;

import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;

import com.google.common.collect.Lists;

public class CallUnmarshallers {

  private List<CallUnmarshaller> unmarshallers = Lists.newArrayList();
  
  public CallUnmarshallers() {
    unmarshallers.add(new PrimitiveUnmarshaller());
    unmarshallers.add(new WrappedPtrUnmarshaller());
  }
  
  
  public void unmarshall(FunctionContext context, GimpleLValue lhs,
      JimpleType returnType, JimpleExpr callExpr) {
    
    if(lhs == null) {
      context.getBuilder().addStatement(callExpr.toString());
    } else {
    
      for(CallUnmarshaller unmarshaller : unmarshallers) {
        if(unmarshaller.unmarshall(context, lhs, returnType, callExpr)) {
          return;
        }
      }
      
      throw new UnsupportedOperationException(String.format("Don't know how to unmarshall call result from [%s] => [%s]",
          returnType.toString(), lhs.toString()));
    }
  }

}
