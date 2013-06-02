package org.renjin.gcc.translate.call;

import java.util.List;

import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.SymbolRef;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.type.ImFunctionPtrType;
import org.renjin.gcc.translate.type.ImFunctionType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImLValue;
import org.renjin.gcc.translate.expr.JvmExprs;
import org.renjin.gcc.translate.type.ImType;
import org.renjin.gcc.translate.var.FunPtrVar;
import org.renjin.gcc.translate.var.Variable;

import com.google.common.collect.Lists;

public class CallTranslator {
  private FunctionContext context;
  private GimpleCall call;

  public CallTranslator(FunctionContext context, GimpleCall call) {
    this.context = context;
    this.call = call;
  }

  public void translate() {
    GimpleExpr functionExpr = call.getFunction();
    if (functionExpr instanceof GimpleAddressOf && 
        ((GimpleAddressOf) functionExpr).getValue() instanceof GimpleFunctionRef) {
      writeStaticCall();

    } else if (functionExpr instanceof SymbolRef) {
      writeFunctionPointerCall();

    } else {
      throw new UnsupportedOperationException(functionExpr.toString());
    }
  }

  /**
   * Write the Jimple necessary to call a static JVM method
   */
  private void writeStaticCall() {
    MethodRef method = context.resolveMethod(call);
    StringBuilder callExpr = new StringBuilder();
    callExpr.append("staticinvoke").append(method.signature());
    marshalParamList(callExpr, method.getParams());

    writeCall(callExpr, method.getReturnType());
  }

  /**
   * Write the Jimple necessary to make a call to a function pointer
   */
  private void writeFunctionPointerCall() {
    ImFunctionType funPtr = getFunPtrInterface();
    FunPtrVar var = getFunPtrVar();

    StringBuilder call = new StringBuilder();
    call.append("interfaceinvoke ").append(var.getJimpleVariable()).append(".").append(funPtr.jimpleSignature());

    marshalParamList(call, funPtr.getParams());
    
    writeCall(call, funPtr.getReturnType());
  }

  private void writeCall(StringBuilder call, JimpleType returnType) {
    if(returnType.equals(JimpleType.VOID) || this.call.getLhs() == null) {
      context.getBuilder().addStatement(call.toString());
    } else {
      ImLValue lvalue = (ImLValue) context.resolveExpr(this.call.getLhs());
      ImExpr rhs = JvmExprs.toExpr(context, new JimpleExpr(call.toString()), returnType, false);
      lvalue.writeAssignment(context, rhs);
    }
  }
  
  private FunPtrVar getFunPtrVar() {
    ImExpr var = context.lookupVar(call.getFunction());
    if (!(var instanceof FunPtrVar)) {
      throw new UnsupportedOperationException("Function value must be a FunPtrVar, got: " + var);
    }
    return (FunPtrVar) var;
  }

  private ImFunctionType getFunPtrInterface() {
    ImType type = context.resolveExpr(call.getFunction()).type();
    if (!(type instanceof ImFunctionPtrType)) {
      throw new UnsupportedOperationException("Function value must be of type ImFunctionPtrType, got: " + type);
    }
    return ((ImFunctionPtrType) type).baseType();
  }


  private void marshalParamList(StringBuilder callExpr, List<CallParam> params) {
    callExpr.append("(");
    boolean needsComma = false;
    for (JimpleExpr param : marshallParams(params)) {
      if (needsComma) {
        callExpr.append(", ");
      }
      callExpr.append(param.toString());
      needsComma = true;
    }
    callExpr.append(")");
  }

  private List<JimpleExpr> marshallParams(List<CallParam> callParams) {
    List<JimpleExpr> exprs = Lists.newArrayList();
    for (int i = 0; i != call.getParamCount(); ++i) {
      ImExpr sourceExpr = context.resolveExpr(call.getArguments().get(i));
      exprs.add(callParams.get(i).marshall(context, sourceExpr));
    }
    return exprs;
  }

}
