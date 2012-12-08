package org.renjin.gcc.translate.call;

import java.util.List;

import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;

import com.google.common.collect.Lists;

public class ParamMarshallers {

  private List<ParamMarshaller> marshallers = Lists.newArrayList();
  
  public ParamMarshallers() {
    marshallers.add(new PrimitiveParamMarshaller());
    marshallers.add(new WrappedPtrParamMarshaller());
    marshallers.add(new FunPtrMarshaller());
    marshallers.add(new StringConstantToStringMarshaller());
    marshallers.add(new StringConstantToCharPtrMarshaller());
    marshallers.add(new StructPtrMarshaller());
  }
  
  public JimpleExpr marshall(FunctionContext context, GimpleExpr expr, CallParam param) {
    for(ParamMarshaller marshaller : marshallers) {
      try {
        return marshaller.marshall(context, expr, param);
      } catch(CannotMarshallException e) {
        continue;
      }
    }
    throw new UnsupportedOperationException(String.format(
        "Unsupported parameter marshalling: [%s: %s] => [%s]", expr.toString(), expr.getClass().getSimpleName(), param.toString()));
  }
  
}
